package de.doridian.crtdemo.basic.tokens.flow;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("GOTO")
public class GOTOToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$goto(" + getAsAssignmentParameters(0) + ");";
    }
}
