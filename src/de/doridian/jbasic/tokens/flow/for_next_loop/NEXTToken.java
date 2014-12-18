package de.doridian.jbasic.tokens.flow.for_next_loop;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "NEXT", noGroups = true)
public class NEXTToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$goto($loopQueue.pop().start);";
    }
}
