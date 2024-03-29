package de.doridian.jbasic.tokens.io;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("GETCHAR")
public class GETCHARToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + parametersSplitDetailed[0].subParams[0].getAsParameter() + " = $io.readChar();";
    }
}
