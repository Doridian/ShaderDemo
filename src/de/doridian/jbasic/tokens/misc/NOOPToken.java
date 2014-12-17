package de.doridian.jbasic.tokens.misc;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName(value = {"REM", "NOOP"}, ignoreParameters = true)
public class NOOPToken extends AbstractToken {
    @Override
    public void insert() {
        program.addNoopLine(line);
    }

    @Override
    public String getCode(String prefix) {
        return null;
    }
}
