package de.doridian.jbasic.tokens.io;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("CLS")
public class CLSToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$io.clearScreen();";
    }
}
