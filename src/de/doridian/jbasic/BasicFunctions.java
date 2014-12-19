package de.doridian.jbasic;

import java.util.regex.Pattern;

public class BasicFunctions {
    private BasicFunctions() { }

    public static int LEN(String str) {
        if(str == null)
            return -1;
        return str.length();
    }
    public static int LEN(Object[] array) {
        return array.length;
    }
    public static int LEN(int[] array) {
        return array.length;
    }

    public static String LEFT$(String str, int num) {
        if(num > str.length())
            num = str.length();
        return str.substring(0, num);
    }
    public static String RIGHT$(String str, int num) {
        if(num > str.length())
            num = str.length();
        return str.substring(str.length() - num, str.length());
    }
    public static String MID$(String str, int start, int len) {
        if(start >= str.length())
            return "";
        if(len + start > str.length())
            len = str.length() - start;
        return str.substring(start, start + len);
    }

    public static int ORD(String str, int num) {
        return str.charAt(num);
    }
    public static int ORD(String str) {
        return ORD(str, 0);
    }
    public static String CHR$(int chr) {
        return "" + ((char)chr);
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
