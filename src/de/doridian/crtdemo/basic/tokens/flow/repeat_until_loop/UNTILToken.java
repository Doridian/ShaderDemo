package de.doridian.crtdemo.basic.tokens.flow.repeat_until_loop;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("UNTIL")
public class UNTILToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$goto($loopQueue.poll().start);";
    }
}
