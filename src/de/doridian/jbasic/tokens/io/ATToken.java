package de.doridian.jbasic.tokens.io;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("AT")
public class ATToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$io.setCursor(" + getAsAssignmentParameters(0) + ");";
    }
}
