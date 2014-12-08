package de.doridian.crtdemo.basic.tokens.io;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "INPUT")
public class INPUTToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        String ret = "";

        if(parametersSplitDetailed.length > 1)
            for(int i = 0; i < parametersSplitDetailed.length - 1; i++)
                ret += prefix + "$io.print(" + parametersSplitDetailed[i] + ");\n";

        return ret + prefix + parametersSplitDetailed[parametersSplitDetailed.length - 1] + " = $io.getLine();";
    }
}
