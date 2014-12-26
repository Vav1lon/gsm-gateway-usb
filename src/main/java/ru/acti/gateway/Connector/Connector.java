package ru.acti.gateway.Connector;

import ru.acti.entity.SmsServiceException;

public interface Connector {

    void sendMessage() throws SmsServiceException;

}
