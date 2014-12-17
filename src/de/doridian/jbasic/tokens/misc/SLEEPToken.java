package de.doridian.jbasic.tokens.misc;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("SLEEP")
public class SLEEPToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "java.lang.Thread.sleep(" + getAsAssignmentParameters(0) + ");";
    }
}
