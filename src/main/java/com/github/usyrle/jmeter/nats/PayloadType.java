package com.github.usyrle.jmeter.nats;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PayloadType {
    BASE64("base64", 0),
    HEX("hex", 1),
    ;

    @Getter
    private final String name;

    @Getter
    private final int index;

    public static String[] getPayloadTypeArray() {
        final String[] payloadTypes = new String[2];
        payloadTypes[BASE64.index] = BASE64.name;
        payloadTypes[HEX.index] = HEX.name;
        return payloadTypes;
    }
}
