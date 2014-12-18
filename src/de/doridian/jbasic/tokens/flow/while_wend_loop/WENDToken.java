package de.doridian.jbasic.tokens.flow.while_wend_loop;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "WEND", noGroups = true)
public class WENDToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$goto($loopQueue.pop().start);";
    }
}
