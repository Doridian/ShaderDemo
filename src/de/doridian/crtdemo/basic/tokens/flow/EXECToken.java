package de.doridian.crtdemo.basic.tokens.flow;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("EXEC")
public class EXECToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return "$execSubFile(" + parametersSplitDetailed[0].subParams[0] + ");";
    }
}
