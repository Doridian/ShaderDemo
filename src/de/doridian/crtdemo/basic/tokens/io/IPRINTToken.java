package de.doridian.crtdemo.basic.tokens.io;

import de.doridian.crtdemo.basic.parameters.GroupedParameter;
import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("IPRINT")
public class IPRINTToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        String ret = "";
        for(GroupedParameter param : parametersSplitDetailed) {
            ret += prefix + "$io.print(" + param.getAsParameter() + ", true);\n";
        }
        return ret.substring(0, ret.length() - 1);
    }
}
