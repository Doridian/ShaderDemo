package de.doridian.jbasic.tokens.io;

import de.doridian.jbasic.parameters.AbstractParameter;
import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("PRINT")
public class PRINTToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return getPrintParameters(prefix, parametersSplitDetailed, false);
    }

    static String getPrintParameters(String prefix, AbstractParameter[] parameter, boolean invert) {
        return getPrintParameters(prefix, parameter, invert, 0, parameter.length);
    }

    static String getPrintParameters(String prefix, AbstractParameter[] parameter, boolean invert, int start, int end) {
        if(end <= start)
            return "";
        String ret = "";
        for(int i = start; i < end; i++)
            ret += prefix + "$io.print(" + parameter[i].getAsParameter() + ", " + (invert ? "true" : "false") + ");\n";
        return ret.substring(0, ret.length() - 1);
    }
}
