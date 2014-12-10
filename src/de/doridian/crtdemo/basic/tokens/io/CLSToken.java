package de.doridian.crtdemo.basic.tokens.io;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("CLS")
public class CLSToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$io.clearScreen();";
    }
}
