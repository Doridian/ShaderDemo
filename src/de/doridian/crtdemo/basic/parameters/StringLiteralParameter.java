package de.doridian.crtdemo.basic.parameters;

import org.apache.commons.lang3.StringEscapeUtils;

public class StringLiteralParameter extends AbstractParameter {
    public StringLiteralParameter(String preChars, String parameter) {
        super(preChars, StringEscapeUtils.unescapeJava(parameter));
    }

    @Override
    public String getAsParameter() {
        return "\"" + StringEscapeUtils.escapeJava((String)value) + "\"";
    }
}
