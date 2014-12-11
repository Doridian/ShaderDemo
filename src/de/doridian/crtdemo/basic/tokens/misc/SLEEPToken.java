package de.doridian.crtdemo.basic.tokens.misc;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("SLEEP")
public class SLEEPToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "java.lang.Thread.sleep(" + parametersSplitDetailed[0].subParams[0].getAsParameter() + ");";
    }
}
