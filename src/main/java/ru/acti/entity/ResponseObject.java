package ru.acti.entity;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import ru.acti.utils.serializer.EnumSerializer;

public class ResponseObject {

    private ResponseStatus status;

    @JsonSerialize(using = EnumSerializer.class)
    private ErrorCodeEnum code;
    private String message;

    public ResponseObject() {
    }

    public ResponseObject(ResponseStatus status) {
        this.status = status;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public ErrorCodeEnum getCode() {
        return code;
    }

    public void setCode(ErrorCodeEnum code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
