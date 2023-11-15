package com.rurbisservices.springreactive.weather.utils;

import java.util.List;

import static com.rurbisservices.springreactive.weather.utils.Constants.EMPTY_STRING;

public class ServiceUtils {
    public static boolean isStringNullOrEmpty(String string) {
        return string == null || EMPTY_STRING.equals(string);
    }

    public static boolean isStringNumber(String string) {
        try {
            Double.parseDouble(string);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Double convertStringToNumber(String string) {
        return Double.parseDouble(string);
    }

    public static boolean isListNullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
}
