package de.doridian.crtdemo.basic;

import java.util.regex.Pattern;

public class BasicFunctions {
    private BasicFunctions() { }

    public static int LEN(String str) {
        return str.length();
    }

    public static String LEFT$(String str, int num) {
        return str.substring(0, num);
    }

    private final static Pattern LTRIM = Pattern.compile("^\\s+");
    private final static Pattern RTRIM = Pattern.compile("\\s+$");

    public static String LTRIM$(String s) {
        return LTRIM.matcher(s).replaceAll("");
    }

    public static String RTRIM$(String s) {
        return RTRIM.matcher(s).replaceAll("");
    }
}
