package de.doridian.jbasic.tokens.io;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("INPUT")
public class INPUTToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        String variable = parametersSplitDetailed[parametersSplitDetailed.length - 1].getAsParameter();

        String func = " = $io.readLine();";
        if(variable.indexOf('$') < 0)
            func = " = Integer.parseInt($io.readLine());";

        return PRINTToken.getPrintParameters(prefix, parametersSplitDetailed, false, 0, parametersSplitDetailed.length - 1)
                + prefix + variable + func;
    }
}
