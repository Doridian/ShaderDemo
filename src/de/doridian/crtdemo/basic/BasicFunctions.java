package de.doridian.crtdemo.basic;

public class BasicFunctions {
    private BasicFunctions() { }

    public static int LEN(String str) {
        return str.length();
    }

    public static String LEFT$(String str, int num) {
        return str.substring(0, num);
    }
}
