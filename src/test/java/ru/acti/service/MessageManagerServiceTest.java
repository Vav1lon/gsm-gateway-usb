package ru.acti.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import ru.acti.entity.SmsServiceException;
import ru.acti.entity.internal.OutgoingSms;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/context-test.xml", "/database-context.xml"})
@Transactional
public class MessageManagerServiceTest {

    @Autowired
    private MessageManagerService messageManagerService;

    @Autowired
    private SessionFactory sessionFactory;



    @Test
    public void NormalizationPhoneNumberTest() throws SmsServiceException {

        messageManagerService.send("89199667268", "test");
        messageManagerService.send("79199667268", "test");
        messageManagerService.send("79199667268846", "test");
        messageManagerService.send("9199667268846", "test");

        List<OutgoingSms> smsList = getSession().createQuery("FROM OUTGOING_SMS").list();

        Assert.assertEquals(4, smsList.size());

        for (OutgoingSms sms : smsList) {

            Assert.assertTrue(sms.getSms().getPhoneNumber().length() < 12 && sms.getSms().getPhoneNumber().length() > 10);
            Assert.assertTrue(sms.getSms().getPhoneNumber().startsWith("7"));
        }

    }

    @Test(expected = SmsServiceException.class)
    public void SendNullTextTest() throws SmsServiceException {
        messageManagerService.send("12345678901", null);
    }

    @Test(expected = SmsServiceException.class)
    public void SendEmptyTextTest() throws SmsServiceException {
        messageManagerService.send("12345678901", "");
    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

}
