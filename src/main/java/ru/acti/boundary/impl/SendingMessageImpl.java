package ru.acti.boundary.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.acti.boundary.SendingMessage;
import ru.acti.entity.ResponseObject;
import ru.acti.entity.ResponseStatus;
import ru.acti.entity.SmsServiceException;
import ru.acti.service.MessageManagerService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/send/")
@Component("sendingMessage")
@Produces(MediaType.APPLICATION_JSON)
public class SendingMessageImpl implements SendingMessage {

    @Autowired
    private MessageManagerService messageManagerService;

    @POST
    @Path("/")
    @Override
    public ResponseObject sendSms(@QueryParam("phoneNumber") String phoneNumber, @QueryParam("text") String text) throws SmsServiceException {
        messageManagerService.send(phoneNumber, text);
        return new ResponseObject(ResponseStatus.OK);
    }

    @GET
    @Path("/balance")
    @Override
    public ResponseObject sendRequestBalance() throws SmsServiceException {
        messageManagerService.requestBalance();
        return new ResponseObject(ResponseStatus.OK);
    }
}
