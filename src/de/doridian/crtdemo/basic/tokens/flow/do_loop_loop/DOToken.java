package de.doridian.crtdemo.basic.tokens.flow.do_loop_loop;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("DO")
public class DOToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$addLoop(" + line + ", " + endingToken.line + ");";
    }

    public Class<? extends AbstractToken> getEndingToken() {
        return LOOPToken.class;
    }
}
