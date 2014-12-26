package ru.acti.entity;

public enum ModemCommand {

    // Service command
    AT("AT", 1),
    OK("OK", 2),
    ERROR("ERROR", 3),
    NO_CARRIER("NO CARRIER", 4),
    ENABLE_PDU("AT+CMFG=0", 5),
    DISABLE_PDU("AT+CMFG=1", 6),


    // Outgoing group
    SEND_SMS("AT+CMGS=", 30),
    SEND_BALANCE_PUSH("AT+CUSD=1,#100#", 31),

    // Incoming group
    NOTIFICATION_INCOMING_SMS("+CMIT", 50),
    GET_INCOMING_SMS("AT+CMGR", 51),
    RING("RING", 52),
    INCOMING_PUSH("+CUSD:", 53),

    //Checks
    CHECK_PUSH("AT+CUSD=?", 60),

    // Other
    BAN_INCOMING_CALL("AT+GSMBUSY=", 70),
    DELETE_READ_SMS("AT+CMGDA=\"DEL READ\"", 71),
    DELETE_UNREAD_SMS("AT+CMGDA=\"DEL UNREAD\"", 72),
    DELETE_SENT_SMS("AT+CMGDA=\"DEL SENT\"", 73),
    DELETE_UNSENT_SMS("AT+CMGDA=\"DEL UNSENT\"", 74),
    DELETE_INBOX_SMS("AT+CMGDA=\"DEL INBOX\"", 75),
    DELETE_ALL_SMS("AT+CMGDA=\"DEL ALL\"", 76), ;

    private String command;
    private int code;

    ModemCommand(String command, int code) {
        this.command = command;
        this.code = code;
    }

    public String getCommand() {
        return command;
    }

    public int getCode() {
        return code;
    }

    public static ModemCommand fromString(String name) {
        for (ModemCommand type : ModemCommand.values()) {
            if (type.getCommand().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("ModemCommand not found by command : " + name);
    }

    public static ModemCommand containsString(String name) {
            for (ModemCommand type : ModemCommand.values()) {
                if (name.contains(type.getCommand())) {
                    return type;
                }
            }
        throw new IllegalArgumentException("ModemCommand not found by command : " + name);
    }

    public static ModemCommand fromInteger(Integer code) {
        for (ModemCommand type : ModemCommand.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("ModemCommand not found by code : " + code);
    }

}
