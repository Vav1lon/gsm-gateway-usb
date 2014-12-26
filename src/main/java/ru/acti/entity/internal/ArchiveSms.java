package ru.acti.entity.internal;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Entity(name = "ARCHIVE_SMS")
public class ArchiveSms implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private Sms sms;

    @Column(name = "INCOMING", nullable = false)
    private Boolean incoming;

    @Column(name = "OUTGOING", nullable = false)
    private Boolean outgoing;

    @Column(name = "CREATED_DATE", nullable = false)
    private Date createdDate = new Date(new java.util.Date().getTime());

    @Column(name = "OPERATION_CODE")
    private Integer operationCode;

    public void setCurrentTime() {
        createdDate = new Date(new java.util.Date().getTime());
    }

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

    public Boolean getIncoming() {
        return incoming;
    }

    public void setIncoming(Boolean incoming) {
        this.incoming = incoming;
    }

    public Boolean getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(Boolean outgoing) {
        this.outgoing = outgoing;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(Integer operationCode) {
        this.operationCode = operationCode;
    }

}
