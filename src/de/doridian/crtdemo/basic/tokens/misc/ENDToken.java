package de.doridian.crtdemo.basic.tokens.misc;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "END", ignoreParameters = true)
public class ENDToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$goto(null);\n" + prefix + "$cleanExit = true;";
    }
}
