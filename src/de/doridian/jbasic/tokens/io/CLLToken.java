package de.doridian.jbasic.tokens.io;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("CLL")
public class CLLToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$io.clearLine(" + getAsAssignmentParameters(0) + ");";
    }
}
