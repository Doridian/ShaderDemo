package de.doridian.crtdemo.basic.tokens.flow.repeat_until_loop;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "UNTIL", noGroups = true)
public class UNTILToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$goto($loopQueue.poll().start);";
    }
}
