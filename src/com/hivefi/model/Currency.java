package com.hivefi.model;

public enum Currency {
    USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, INR, MXN;

    public static boolean isSupported(String code) {
        try {
            valueOf(code.toUpperCase());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
