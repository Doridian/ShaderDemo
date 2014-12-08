package de.doridian.crtdemo.basic.tokens.flow.while_wend_loop;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("WEND")
public class WENDToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$goto($loopQueue.poll().start);";
    }
}
