package de.doridian.crtdemo.basic.tokens.io;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("GETCHAR")
public class GETCHARToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + parametersSplitDetailed[parametersSplitDetailed.length - 1].subParams[0].getAsParameter() + " = $io.readChar();";
    }
}
