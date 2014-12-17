package de.doridian.jbasic.parameters;

import org.apache.commons.lang3.StringEscapeUtils;

public class StringLiteralParameter extends AbstractParameter {
    public StringLiteralParameter(String parameter) {
        super(StringEscapeUtils.unescapeJava(parameter));
    }

    @Override
    public String getAsParameter() {
        return "\"" + StringEscapeUtils.escapeJava((String)value) + "\"";
    }
}
