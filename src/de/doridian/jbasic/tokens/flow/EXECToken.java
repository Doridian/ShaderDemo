package de.doridian.jbasic.tokens.flow;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("EXEC")
public class EXECToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return "$execSubFile(" + parametersSplitDetailed[0].subParams[0] + ");";
    }
}
