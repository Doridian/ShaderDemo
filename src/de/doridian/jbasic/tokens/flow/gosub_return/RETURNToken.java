package de.doridian.jbasic.tokens.flow.gosub_return;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "RETURN", ignoreParameters = true)
public class RETURNToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$gotoAfter($callQueue.pop());";
    }
}
