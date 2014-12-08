package de.doridian.crtdemo.basic.tokens.misc;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName({"REM", "NOOP"})
public class NOOPToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return " ";
    }
}
