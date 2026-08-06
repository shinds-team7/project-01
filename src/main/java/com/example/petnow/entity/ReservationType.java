package com.example.petnow.entity;

public enum ReservationType {
    DAY_USE("당일"),
    OVERNIGHT("숙박");

    private final String databaseValue;

    ReservationType(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue() {
        return databaseValue;
    }
}
