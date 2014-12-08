package de.doridian.crtdemo.basic.tokens.flow.for_next_loop;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("NEXT")
public class NEXTToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$gotoAfter($loopQueue.poll().start);";
    }
}
