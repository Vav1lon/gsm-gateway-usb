package ru.acti.entity.internal;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "INCOMING_SMS")
public class IncomingSms implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private Sms sms;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sms getSms() {
        return sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms;
    }
}
