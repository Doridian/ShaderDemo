package de.doridian.jbasic.tokens.flow.repeat_until_loop;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "UNTIL", noGroups = true)
public class UNTILToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$goto($loopQueue.pop().start);";
    }
}
