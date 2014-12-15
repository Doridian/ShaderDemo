package de.doridian.crtdemo.basic.tokens.io;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("CLL")
public class CLLToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$io.clearLine(" + getAsAssignmentParameters(0) + ");";
    }
}
