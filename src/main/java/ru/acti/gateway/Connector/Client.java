package ru.acti.gateway.Connector;

public interface Client {

    void sendSms(String phoneNumber, String smsText);

}
