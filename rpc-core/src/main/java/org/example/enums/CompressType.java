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
public enum CompressType {
    GZIP((byte) 1, "gzip"),
    ;

    private final byte code;
    private final String desc;


    public static CompressType from(byte code) {
        return Arrays.stream(values())
            .filter(o -> o.code == code)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("code异常: " + code));
    }
}
