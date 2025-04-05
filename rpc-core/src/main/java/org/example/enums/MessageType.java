package org.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

/**
 * @Author Mike
 * @Date 2025/4/1
 **/
@Getter
@ToString
@AllArgsConstructor
public enum MessageType {
    HEARTBEAT_REQ((byte) 1, "心跳请求"),
    HEARTBEAT_RESP((byte) 2, "心跳响应"),
    RPC_REQ((byte) 3, "rpc请求"),
    RPC_RESP((byte) 4, "rpc响应");

    private final byte code;
    private final String desc;

    public boolean isHeartbeat() {
        return this == HEARTBEAT_RESP || this == HEARTBEAT_REQ;
    }

    public boolean isReq() {
        return this == RPC_REQ || this == HEARTBEAT_REQ;
    }

    public static MessageType from(byte code) {
        return Arrays.stream(values())
            .filter(messageType -> messageType.code == code)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("code异常: " + code));
    }
}
