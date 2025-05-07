package com.example.cricket_app.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Team {
    INDIA,
    AUSTRALIA,
    ENGLAND,
    PAKISTAN;

    @JsonCreator
    public static Team fromString(String value) {
        return Team.valueOf(value.toUpperCase());
    }//it is return to avoid serialization error.

    @JsonValue
    public String getValue() {
        return this.name();
    }
}
