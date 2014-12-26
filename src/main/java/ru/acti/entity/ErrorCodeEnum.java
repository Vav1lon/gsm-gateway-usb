package ru.acti.entity;

public enum ErrorCodeEnum {

    FATAl_ERROR(0),
    NOT_CORRECT_PARAMS(10);

    private int code;

    public int getCode() {
        return code;
    }

    private ErrorCodeEnum(int code) {
        this.code = code;
    }

    public static ErrorCodeEnum fromInt(int id) {
        for (ErrorCodeEnum type : ErrorCodeEnum.values()) {
            if (type.getCode() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("ErrorCodeEnum not found by id : " + id);
    }

}
