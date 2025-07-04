package com.wornux.features.client.domain;

import lombok.Getter;

@Getter
public enum PreferredContactMethod {
    WHATSAPP("WhatsApp"), SMS("SMS"), EMAIL("Correo electrónico"), PHONE_CALL("Llamada telefónica");

    private final String description;

    PreferredContactMethod(String description) {
        this.description = description;
    }

}