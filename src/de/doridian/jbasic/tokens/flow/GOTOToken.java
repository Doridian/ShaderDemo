package de.doridian.jbasic.tokens.flow;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("GOTO")
public class GOTOToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$goto(" + getAsAssignmentParameters(0) + ");";
    }
}
