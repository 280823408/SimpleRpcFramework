package org.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

/**
 * @Author Mike
 * @Date 2025/4/1
 **/
@ToString
@Getter
@AllArgsConstructor
public enum VersionType {
    VERSION1((byte) 1, "版本1");

    private final byte code;
    private final String desc;

    public static VersionType from(byte code) {
        return Arrays.stream(values())
            .filter(o -> o.code == code)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("code异常: " + code));
    }
}
