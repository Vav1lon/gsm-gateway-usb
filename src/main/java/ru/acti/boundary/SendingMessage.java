package ru.acti.boundary;

import org.springframework.transaction.annotation.Transactional;
import ru.acti.entity.ResponseObject;
import ru.acti.entity.SmsServiceException;

import javax.jws.WebService;

@WebService
@Transactional
public interface SendingMessage {

    ResponseObject sendSms(String phoneNumber, String text) throws SmsServiceException;
    ResponseObject sendRequestBalance() throws SmsServiceException;

}
