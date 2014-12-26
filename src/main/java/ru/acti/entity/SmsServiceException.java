package ru.acti.entity;

public class SmsServiceException extends Exception {

    private ErrorCodeEnum errorCodeEnum;

    public SmsServiceException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public SmsServiceException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public SmsServiceException(String message, ErrorCodeEnum errorCodeEnum) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
        this.errorCodeEnum = errorCodeEnum;
    }

    public SmsServiceException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public SmsServiceException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected SmsServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ErrorCodeEnum getErrorCodeEnum() {
        return errorCodeEnum;
    }
}
