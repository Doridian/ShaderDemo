package de.doridian.jbasic.tokens.flow.do_loop_loop;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "DO", noGroups = true)
public class DOToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$addLoop(" + line + ", " + endingToken.line + ");";
    }

    public Class<? extends AbstractToken> getEndingToken() {
        return LOOPToken.class;
    }
}
