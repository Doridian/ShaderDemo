package de.doridian.crtdemo.basic.tokens.flow.while_wend_loop;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "WHILE", noGroups = true)
public class WHILEToken extends AbstractToken {
    @Override
    public void insert() {
        addLine("\t\tif(" + getAsConditionalParameters(0, true) + ") {\n\t\t\t$gotoAfter(" + endingToken.line + ");\n\t\t\treturn;\n\t\t}\n\t\t$addLoop(" + line + ", " + endingToken.line + ");");
    }

    public Class<? extends AbstractToken> getEndingToken() {
        return WENDToken.class;
    }

    @Override
    public String getCode(String prefix) {
        return null;
    }
}
