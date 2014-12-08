package de.doridian.crtdemo.basic.tokens.io;

import de.doridian.crtdemo.basic.parameters.AbstractParameter;
import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "PRINT")
public class PRINTToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        String ret = "";
        for(AbstractParameter param : parametersSplitDetailed) {
            ret += prefix + "$io.print(" + param + ");\n";
        }
        return ret.substring(0, ret.length() - 1);
    }
}
