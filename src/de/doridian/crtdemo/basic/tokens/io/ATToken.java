package de.doridian.crtdemo.basic.tokens.io;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("AT")
public class ATToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$io.setCursor(" + parametersSplitDetailed[0].subParams[0] + ", " + parametersSplitDetailed[1].subParams[0] + ");";
    }
}
