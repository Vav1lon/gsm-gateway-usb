package ru.acti.gateway.Listener;

import com.google.common.base.Strings;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.acti.entity.ModemCommand;
import ru.acti.entity.TypeMessage;
import ru.acti.gateway.Connector.impl.ConnectorImpl;

import java.io.IOException;
import java.io.InputStream;

public class SmsListener implements SerialPortEventListener {

    private static final Logger LOGGER = Logger.getLogger(SmsListener.class);

    @Autowired
    private ConnectorImpl connectorImpl;

    @Override
    public synchronized void serialEvent(SerialPortEvent serialPortEvent) {

        connectorImpl.setStatusLastCommand(false);

        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

            try {

                String inputCommand = readInputData(serialPortEvent).trim();

                if (!Strings.isNullOrEmpty(inputCommand)) {
                    switch (ModemCommand.containsString(inputCommand)) {
                        case INCOMING_PUSH:
                            LOGGER.info("Incoming push message in GSM Modem");
                            String message = readUSSDMessage(inputCommand);

                            //TODO: remove, need for Debug
                            LOGGER.info("Incoming message: " + message);
                            break;
                        case NOTIFICATION_INCOMING_SMS:
                            LOGGER.info("Incoming sms in GSM Modem");
                            connectorImpl.incoming(TypeMessage.SMS, null);
                            break;
                        case RING:
                            LOGGER.info("Incoming call in GSM Modem");
                            break;
                        case OK:
                            connectorImpl.setStatusLastCommand(true);
                            break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    private String readUSSDMessage(String inputCommand) {
        Integer codePage = parseCodeCharset(inputCommand);

        String message = null;
        String originalString = parseTextMessage(inputCommand);

        if (codePage == 72) {
            message = readDataToString(originalString);
        } else {
            message = originalString;
        }
        return message;
    }

    private String parseTextMessage(String inputCommand) {
        return inputCommand.substring(inputCommand.indexOf("\"") + 1, inputCommand.lastIndexOf("\""));
    }

    private Integer parseCodeCharset(String inputCommand) {
        return Integer.parseInt(inputCommand.substring(inputCommand.lastIndexOf(",") + 1).trim());
    }

    private String readDataToString(String inputCommand) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < inputCommand.length(); i += 4) {
            stringBuilder.append((char) Integer.parseInt(inputCommand.substring(i, i + 4), 16));
        }
        return stringBuilder.toString();
    }

    private String readInputData(SerialPortEvent serialPortEvent) throws IOException {
        SerialPort serialPort = (SerialPort) serialPortEvent.getSource();
        return readInputBuffer(serialPort.getInputStream());
    }

    private String readInputBuffer(InputStream inputStream) throws IOException {

        byte[] bytes = new byte[1024];
        int i = 0;

        while (true) {
            if (inputStream.available() > 0) {
                int c = inputStream.read(bytes);
                if (c != 13) {
                    bytes[i] = (byte) c;
                } else {
                    break;
                }
                i++;
            }
        }

        String result = new String(bytes);

        return Strings.isNullOrEmpty(result) ? null : result;
    }
}
