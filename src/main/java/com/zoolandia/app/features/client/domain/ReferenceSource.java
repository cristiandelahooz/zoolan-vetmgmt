package com.zoolandia.app.features.client.domain;

import lombok.Getter;

@Getter
public enum ReferenceSource {
    REFERIDO_CLIENTE("Referido por otro cliente"),
    REDES_SOCIALES("Redes sociales"),
    PUBLICIDAD("Publicidad"),
    GOOGLE("Búsqueda en Google"),
    PASANTE("Paso por la clínica"),
    RECOMENDACION_PROFESIONAL("Recomendación profesional"),
    OTRO("Otro");

    private final String description;

    ReferenceSource(String description) {
        this.description = description;
    }

}