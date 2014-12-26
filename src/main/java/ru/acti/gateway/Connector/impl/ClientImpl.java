package ru.acti.gateway.Connector.impl;

import gnu.io.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import ru.acti.entity.ModemCommand;
import ru.acti.gateway.Connector.Client;
import ru.acti.gateway.Listener.SmsListener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.TooManyListenersException;

//@Component("client")
public class ClientImpl implements Client {

    @Value("${smsservice.client.EOL}")
    private byte EOL;

    @Value("${smsservice.client.CtrlZ}")
    private byte CtrlZ;

    @Value("${smsservice.client.Cancel}")
    private byte Cancel;

    @Value("${smsservice.client.modemTimeout}")
    private int modemTimeout;

    @Value("${smsservice.client.modemBaudrate}")
    private int modemBaudrate;

    @Value("${smsservice.client.modemTryCount}")
    private int modemTryCount;

    @Value("${smsservice.client.modemTryWait}")
    private int modemTryWait;

    @Value("${smsservice.client.smsTimeout}")
    private int smsTimeout;

    @Value("${smsservice.client.smsTryCount}")
    private int smsTryCount;

    @Value("${smsservice.client.smsTryWait}")
    private int smsTryWait;

    @Value("${smsservice.client.portName}")
    private String portName;

    @Value("${smsservice.client.portTimeout}")
    private int portTimeout;

    @Value("${smsservice.client.portTryCount}")
    private int portTryCount;

    @Value("${smsservice.client.portTryWait}")
    private int portTryWait;

    private InputStream in = null;
    private OutputStream out = null;
    private SerialPort serialPort = null;

    private static Logger logger = Logger.getLogger(ClientImpl.class);

    @PostConstruct
    private void init() {
        initModem();
        logger.info("Служба отправки SMS запущена");
    }

    @PreDestroy
    private void close() {
        closeModem();
        logger.info("Служба отправки SMS остановлена");
    }

    public void sendSms(String phoneNumber, String smsText) {
        // если надо - вынести в параметры
        boolean msgFlashBlinked = false; // flashSMS (мигание)
        boolean msgFlash = false; // flashSMS

        String phone = phoneNumber;

        // добить длину телефона до 12 символов
        while (phone.length() < 12) {
            phone += "F";
        }

        byte[] msgb = null;

        try {
            msgb = smsText.getBytes("UTF-16"); // кодировать SMS сообщение в UTF-16
        } catch (UnsupportedEncodingException e) {
            logger.error("Ошибка кодировки приотправке SMS на номер " + phoneNumber + ". Сообщение не отправлено");
            return;
        }


        String msgPacked = ""; // закодированный ТЕКСТ сообщения

        // сообщение должно быть в 16-ричной системе (не ASCII-представления!!!)
        for (int i = 2; i < msgb.length; i++) { // первые 2 байта не берём, они, явовские системные
            String b = Integer.toHexString((int) msgb[i]);
            if (b.length() < 2)
                msgPacked += "0";
            msgPacked += b;
        }

        String smsb = ""; // сообщение ЦЕЛИКОМ (текст + доп. информация)

        smsb += "00"; // smsc - default - брать номер SMS центра из настроек GSM
        smsb += "11"; // sms-submit - [0001 - 0001] - [установка формата и длины поля Validity Period - тип SMS-submit (от телефона к центру)]
        smsb += "00"; // reference - "00" - признак того, что в качестве адреса отправителя используется номер телефона

        String dpLength = Integer.toHexString(phoneNumber.length()); // длина номера телефона

        if (dpLength.length() < 2)
            smsb += "0";

        smsb += dpLength;

        smsb += "91"; // address - int number - тип адреса (91 - в качестве адреса используется номер телефона)
        smsb += phone.charAt(1); // phone - перекодировать номер (соседние разряды меняются местами)
        smsb += phone.charAt(0);
        smsb += phone.charAt(3);
        smsb += phone.charAt(2);
        smsb += phone.charAt(5);
        smsb += phone.charAt(4);
        smsb += phone.charAt(7);
        smsb += phone.charAt(6);
        smsb += phone.charAt(9);
        smsb += phone.charAt(8);
        smsb += phone.charAt(11);
        smsb += phone.charAt(10);

        smsb += "00"; // идентификатор протокола (00 - всё по-умолчанию, кажется)
        if (msgFlash)
            smsb += "1";
        else
            smsb += "0"; // flash message

        smsb += "8"; // encoding: ucs2 - кодировка UCS-2
        smsb += "A7"; // valid for 1 day - срок действия 1 день

        if (msgFlashBlinked)
            msgPacked = "0001" + msgPacked; // flashSMS

        String msglenPacked = Integer.toHexString(msgPacked.length() / 2); // длина пакета

        if (msglenPacked.length() < 2)
            smsb += "0";

        smsb += msglenPacked;
        smsb += msgPacked;

        smsb = smsb.toUpperCase();

        for (int i = 0; i < smsTryCount; i++) {
            writeModem("AT+CMGS=" + Integer.toString(smsb.length() / 2 - 1)); // отправить длину PDU-пакета
            if (!waitModem(">")) {
                logger.warn("Ошибка отправки длины пакета. Осталось попыток: " + (smsTryCount - i - 1));
                writeModem(Cancel);
                try {
                    Thread.sleep(modemTryWait);
                } catch (InterruptedException ex) {
                }
            } else {
                writeModem(smsb); // отправить PDU-пакет
                writeModem(CtrlZ);
                //writeModem(Cancel);
                if (!waitModem("OK", smsTimeout)) { // подождём
                    logger.warn("Ошибка отправки SMS на номер " + phoneNumber + ". Осталось попыток: " + (smsTryCount - i - 1));
                    writeModem(Cancel);
                    try {
                        Thread.sleep(smsTryWait);
                    } catch (InterruptedException ex) {
                    }
                } else {
                    return;
                }
            }
        }
        logger.error("Ошибка отправки SMS на номер " + phoneNumber + ". Сообщение не отправлено");
    }

    private boolean waitModem(String forWhat) {
        return waitModem(forWhat, modemTimeout);
    }

    private boolean waitModem(String forWhat, long timeout) {
        String response = "";
        for (int i = 0; i < timeout / 10; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
            response += readModem();
            if (response.contains(forWhat))
                return true;
            if (response.contains("ERROR"))
                return false;
        }
        return false;
    }

    private void writeModem(byte b) {
        for (int i = 0; i < modemTryCount; i++) {
            try {
                out.write(b);
                return;
            } catch (IOException e) {
                logger.warn("Ошибка отправки данных модему. Осталось попыток: " + (modemTryCount - i - 1));
                try {
                    Thread.sleep(modemTryWait);
                } catch (InterruptedException ex) {
                }
            }
        }
        logger.fatal("Не удалось отправить данные модему");
        System.exit(1);
    }

    private void writeModem(String cmd) {
        for (int i = 0; i < modemTryCount; i++) {
            try {
                out.write(cmd.getBytes());
                out.write(EOL);
                return;
            } catch (IOException e) {
                logger.warn("Ошибка отправки данных модему. Осталось попыток: " + (modemTryCount - i - 1));
                try {
                    Thread.sleep(modemTryWait);
                } catch (InterruptedException ex) {
                }
            }
        }
        logger.fatal("Не удалось отправить данные модему");
        System.exit(1);
    }

    private void writeModem1(String cmd) {
        for (int i = 0; i < modemTryCount; i++) {
            try {
                out.write(cmd.getBytes());

                return;
            } catch (IOException e) {
                logger.warn("Ошибка отправки данных модему. Осталось попыток: " + (modemTryCount - i - 1));
                try {
                    Thread.sleep(modemTryWait);
                } catch (InterruptedException ex) {
                }
            }
        }
        logger.fatal("Не удалось отправить данные модему");
        System.exit(1);
    }

    private String readModem() {
        for (int i = 0; i < modemTryCount; i++) {
            try {
                String response = "";
                byte[] readBuffer = new byte[20];
                while (in.available() > 0) {
                    int numBytes = in.read(readBuffer);
                    response = response.concat(new String(readBuffer).substring(0, numBytes));
                }
                while (response.startsWith("\r") || response.startsWith("\n")) {
                    response = response.substring(1);
                }
                return response;
            } catch (IOException e) {
                logger.warn("Ошибка отправки данных модему. Осталось попыток: " + (modemTryCount - i - 1));
                try {
                    Thread.sleep(modemTryWait);
                } catch (InterruptedException ex) {
                }
            }
        }
        logger.fatal("Не удалось получить данные от модема");
        System.exit(1);
        return null;
    }

    private void initModem() {
        CommPortIdentifier portId = null;
        try {
            // получить идентификатор порта
            portId = CommPortIdentifier.getPortIdentifier(portName);
        } catch (NoSuchPortException e) {
            logger.fatal("Указанный порт не существует");
        }

        if (portId.getPortType() != CommPortIdentifier.PORT_SERIAL) {
            logger.fatal("Порт, указанный в конфигурационном файле (service.conf), не является последовательным портом");
        }

        boolean ok = false;
        for (int i = 0; i < portTryCount; i++) {
            try {
                serialPort = (SerialPort) portId.open("SendSmsService", portTimeout); // открыть порт
                ok = true;
                break;
            } catch (PortInUseException e) {
                logger.warn("Порт занят. Осталось попыток: " + (portTryCount - i - 1));
                try {
                    Thread.sleep(portTryWait);
                } catch (InterruptedException ex) {
                }
            }
        }
        if (!ok) {
            logger.fatal("Не удалось открыть порт: порт занят");
        }

        ok = false;
        for (int i = 0; i < portTryCount; i++) {
            try {
                out = serialPort.getOutputStream(); // выходной поток
                in = serialPort.getInputStream(); // входной поток
                ok = true;
                break;
            } catch (IOException e) {
                logger.warn("Не удалось открыть порт: не удалось открыть поток ввода/вывода. Осталось попыток: " + (portTryCount - i - 1));
                try {
                    Thread.sleep(portTryWait);
                } catch (InterruptedException ex) {
                }
            }
        }
        if (!ok) {
            logger.fatal("Не удалось открыть порт: не удалось открыть поток ввода/вывода");
        }

        ok = false;
        for (int i = 0; i < portTryCount; i++) {
            try {
                // настройки порта (правильно указать скорость!)
                serialPort.setSerialPortParams(modemBaudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                ok = true;
                break;
            } catch (UnsupportedCommOperationException e) {
                logger.warn("Не удалось установить параметры порта. Осталось попыток: " + (portTryCount - i - 1));
                try {
                    Thread.sleep(portTryWait);
                } catch (InterruptedException ex) {
                }
            }
        }
        if (!ok) {
            logger.fatal("Не удалось установить параметры порта");
        }

        serialPort.setDTR(false); // настройки (UART)
        serialPort.setRTS(false); // настройки (UART)

        // сброс, мало ли чем там модем занимался
        writeModem(Cancel);
        readModem();

        ok = false;
        for (int i = 0; i < modemTryCount; i++) {
            writeModem("AT"); // проверка наличия модема
            if (!waitModem("OK")) {
                logger.warn("Модем не отвечает. Осталось попыток: " + (modemTryCount - i - 1));
                try {
                    Thread.sleep(modemTryWait);
                } catch (InterruptedException ex) {
                }
            } else {
                ok = true;
                break;
            }
        }
        if (!ok) {
            logger.fatal("Не удалось получить ответ от модема");
        }

        ok = false;
        for (int i = 0; i < modemTryCount; i++) {
            writeModem("AT+CPIN?"); // проверка готовности модема
            if (!waitModem("CPIN: READY")) {
                logger.warn("Модем не готов. Осталось " + (modemTryCount - i - 1) + " попыток");
                try {
                    Thread.sleep(modemTryWait);
                } catch (InterruptedException ex) {
                }
            } else {
                ok = true;
                break;
            }
        }
        if (!ok) {
            logger.fatal("Не удалось получить подтверждение готовности от модема");
        }

        ok = false;
        for (int i = 0; i < modemTryCount; i++) {
            writeModem("AT+CSMS=0"); // проверка возможности отсылки SMS модемом
            if (!waitModem("OK")) {
                logger.warn("Не удалось получить от медема подтверждение возможности отправки SMS. Осталось попыток: " + (modemTryCount - i - 1));
                try {
                    Thread.sleep(modemTryWait);
                } catch (InterruptedException ex) {
                }
            } else {
                ok = true;
                break;
            }
        }
        if (!ok) {
            logger.fatal("Модем не поддерживает функцию отправки SMS");
        }

        ok = false;
        for (int i = 0; i < modemTryCount; i++) {
            writeModem("AT+CMGF=0"); // установка PDU режима
            if (!waitModem("OK")) {
                logger.warn("Не удалось установить PDU режим. Осталось попыток: " + (modemTryCount - i - 1));
                try {
                    Thread.sleep(modemTryWait);
                } catch (InterruptedException ex) {
                }
            } else {
                ok = true;
                break;
            }
        }
        if (!ok) {
            logger.fatal("Не удалось установить PDU режим");
        }

        try {
            serialPort.addEventListener(new SmsListener());
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        serialPort.notifyOnDataAvailable(true);
    }

    private void closeModem() {
        // закрыть порт
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
    }

    public void sendBalancePush() {

        try {
            sendCommandToModem(ModemCommand.AT);
            sendCommandToModem(ModemCommand.SEND_BALANCE_PUSH);
            sendCommandToModem(ModemCommand.CHECK_PUSH);
        } catch (Exception e) {
          logger.error(e.getMessage());
        }
    }

    private void sendCommandToModem(ModemCommand command) throws IOException {
        out.write(command.getCommand().getBytes());
        out.write(EOL);
    }
}
