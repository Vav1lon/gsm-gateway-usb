package ru.acti.gateway.Connector.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.acti.entity.ModemCommand;
import ru.acti.entity.SmsServiceException;
import ru.acti.entity.TypeMessage;
import ru.acti.entity.internal.ArchiveSms;
import ru.acti.entity.internal.OutgoingSms;
import ru.acti.gateway.Connector.Connector;

import java.util.List;

@Component("connector")
@Transactional(propagation = Propagation.REQUIRED)
public class ConnectorImpl implements Connector {

    private static final Logger LOGGER = Logger.getLogger(ConnectorImpl.class);

    private static final String NOT_FOUND_COMMAND = "Not detect command sending message";

    private boolean statusLastCommand;

    @Autowired
    private SessionFactory sessionFactory;

//    @Autowired
    private ClientImpl clientImpl;

    public synchronized void sendMessage() throws SmsServiceException {

        List<OutgoingSms> smsList = getSession().createQuery("FROM OUTGOING_SMS ").list();

        for (OutgoingSms outgoingSms : smsList) {

            switch (ModemCommand.fromInteger(outgoingSms.getCode())) {
                case SEND_SMS:
                    clientImpl.sendSms(outgoingSms.getSms().getPhoneNumber(), outgoingSms.getSms().getText());
                    break;
                case SEND_BALANCE_PUSH:
                    clientImpl.sendBalancePush();
                    break;
                default:
                    LOGGER.error(NOT_FOUND_COMMAND);
                    throw new SmsServiceException(NOT_FOUND_COMMAND);
            }

            if (isStatusLastCommand()) {
                deleteOutgoingMessageInQueue(outgoingSms);
            }
        }
    }

    public void incoming(TypeMessage typeMessage, String message) {

    }

    public synchronized void checkNewSms() {
    }

    private void deleteOutgoingMessageInQueue(OutgoingSms outgoingSms) {
        moveMessageToArchive(outgoingSms);
        getSession().delete(outgoingSms);
    }

    private void moveMessageToArchive(OutgoingSms sms) {
        ArchiveSms archiveSms = new ArchiveSms();
        archiveSms.setSms(sms.getSms());
        archiveSms.setCurrentTime();
        archiveSms.setIncoming(false);
        archiveSms.setOutgoing(true);
        archiveSms.setOperationCode(sms.getCode());
        getSession().save(archiveSms);
    }

    public boolean isStatusLastCommand() {
        return statusLastCommand;
    }

    public void setStatusLastCommand(boolean statusLastCommand) {
        this.statusLastCommand = statusLastCommand;
    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }
}