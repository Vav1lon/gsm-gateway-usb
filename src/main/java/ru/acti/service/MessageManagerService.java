package ru.acti.service;

import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.acti.entity.ErrorCodeEnum;
import ru.acti.entity.ModemCommand;
import ru.acti.entity.SmsServiceException;
import ru.acti.entity.internal.OutgoingSms;
import ru.acti.entity.internal.Sms;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class MessageManagerService {

    private static final Logger LOGGER = Logger.getLogger(MessageManagerService.class);

    private static final String NOT_A_REQUIRED_PARAMETER = "Не указан один из обязательных параметров: phoneNumber=%s, text=%s";
    private static final String NOT_A_VALID_PHONE_NUMBER_LENGTH = "Не верная длина номера телефона, длина телефона должна быть не менее 10 символов";

    private static final String FIRST_SYMBOL_PHONE_NUMBER_NUMBER = "7";
    private static final int MINIMAL_PHONE_NUMBER_LENGTH = 10;

    @Autowired
    private SessionFactory sessionFactory;

    public void send(String phoneNumber, String smsText) throws SmsServiceException {

        validateIncomingParams(phoneNumber, smsText);

        phoneNumber = normalizationPhoneNumber(phoneNumber);

        addMessageToQueue(phoneNumber, smsText, ModemCommand.SEND_SMS);
    }

    public void requestBalance() {
        OutgoingSms newSms = new OutgoingSms();
        newSms.setCode(ModemCommand.SEND_BALANCE_PUSH.getCode());
        getSession().save(newSms);
    }

    private void addMessageToQueue(String phoneNumber, String smsText, ModemCommand modemCommand) {
        OutgoingSms newSms = new OutgoingSms();
        newSms.setSms(new Sms(phoneNumber, smsText));
        newSms.setCode(modemCommand.getCode());
        getSession().save(newSms);
    }

    private String normalizationPhoneNumber(String phoneNumber) {
        int phoneNumberLength = phoneNumber.length();
        return FIRST_SYMBOL_PHONE_NUMBER_NUMBER + phoneNumber.substring(phoneNumberLength - MINIMAL_PHONE_NUMBER_LENGTH, phoneNumberLength);
    }

    private void validateIncomingParams(String phoneNumber, String smsText) throws SmsServiceException {

        if (Strings.isNullOrEmpty(phoneNumber) || Strings.isNullOrEmpty(smsText)) {
            String message = String.format(NOT_A_REQUIRED_PARAMETER, phoneNumber, smsText);
            LOGGER.error(message);
            throw new SmsServiceException(message, ErrorCodeEnum.NOT_CORRECT_PARAMS);
        }

        if (phoneNumber.length() < MINIMAL_PHONE_NUMBER_LENGTH) {
            LOGGER.error(NOT_A_VALID_PHONE_NUMBER_LENGTH);
            throw new SmsServiceException(NOT_A_VALID_PHONE_NUMBER_LENGTH, ErrorCodeEnum.NOT_CORRECT_PARAMS);
        }

    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }
}
