package de.doridian.crtdemo.basic.tokens.io;

import de.doridian.crtdemo.basic.parameters.GroupedParameter;
import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("IPRINT")
public class IPRINTToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return PRINTToken.getPrintParameters(prefix, parametersSplitDetailed, true);
    }
}
