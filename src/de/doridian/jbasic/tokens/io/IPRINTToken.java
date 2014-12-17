package de.doridian.jbasic.tokens.io;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("IPRINT")
public class IPRINTToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return PRINTToken.getPrintParameters(prefix, parametersSplitDetailed, true);
    }
}
