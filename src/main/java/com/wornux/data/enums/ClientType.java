package com.wornux.data.enums;

import lombok.Getter;

@Getter
public enum ClientType {

    COMMERCIAL("Commercial"), RESIDENTIAL("Residential");

    private final String display;

    ClientType(String display) {
        this.display = display;
    }
}
