package com.mdc.mim.common.dto;

public enum MessageTypeEnum {
    LOGIN_REQ(0), LOGIN_RESP(1), LOGOUT_REQ(2), LOGOUT_RESP(3), KEEP_ALIVE_REQ(4), KEEP_ALIVE_RESP(5), MESSAGE_REQ(6),
    MESSAGE_RESP(7), MESSAGE_NOTIFICATION(8);

    private int val;

    MessageTypeEnum(int val) {
        this.val = val;
    }

    public int value() {
        return val;
    }
}
