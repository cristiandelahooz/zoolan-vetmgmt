package com.wornux.data.enums;

import lombok.Getter;

@Getter
public enum ClientType {

    COMMERCIAL("Comercial"), RESIDENTIAL("Residencial");

    private final String display;

    ClientType(String display) {
        this.display = display;
    }
}
