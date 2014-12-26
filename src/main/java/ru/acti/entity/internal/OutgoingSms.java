package ru.acti.entity.internal;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "OUTGOING_SMS")
public class OutgoingSms implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private Sms sms;

    @Column(name = "CODE", nullable = false)
    private int code;

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

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
