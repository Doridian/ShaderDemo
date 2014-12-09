package de.doridian.crtdemo.basic.tokens.flow.repeat_until_loop;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "REPEAT", noGroups = true)
public class REPEATToken extends AbstractToken {
    @Override
    public void insert() {
        addLine("\t\tif(" + endingToken.getAsConditionalParameters(0, false) + ") {\n\t\t\t$gotoAfter(" + endingToken.line + ");\n\t\t\treturn;\n\t\t}\n\t\t$addLoop(" + line + ", " + endingToken.line + ");");
    }

    public Class<? extends AbstractToken> getEndingToken() {
        return UNTILToken.class;
    }

    @Override
    public String getCode(String prefix) {
        return null;
    }
}
