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

    public static String RIGHT$(String str, int num) {
        return str.substring(str.length() - num, str.length());
    }

    public static String CHR$(String str, int num) {
        return "" + str.charAt(num);
    }

    public static int CHR(String str, int num) {
        return str.charAt(num);
    }

    public static String SUB$(String str, int start, int end) {
        return str.substring(start, end);
    }

    private final static Pattern _LTRIM = Pattern.compile("^\\s+");
    private final static Pattern _RTRIM = Pattern.compile("\\s+$");

    public static String LTRIM$(String s) {
        return _LTRIM.matcher(s).replaceAll("");
    }

    public static String RTRIM$(String s) {
        return _RTRIM.matcher(s).replaceAll("");
    }
}
