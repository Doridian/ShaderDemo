package de.doridian.crtdemo.basic.tokens.io;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("INPUT")
public class INPUTToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        String ret = "";

        String variable = parametersSplitDetailed[parametersSplitDetailed.length - 1].subParams[0].getAsParameter();
        program.addVariable(variable);

        String func = " = $io.readLine();";
        if(variable.charAt(variable.length() - 1) != '$')
            func = " = Integer.parseInt($io.readLine());";

        if(parametersSplitDetailed.length > 1)
            for(int i = 0; i < parametersSplitDetailed.length - 1; i++)
                ret += prefix + "$io.print(" + parametersSplitDetailed[i].getAsParameter() + ");\n";

        return ret + prefix + variable + func;
    }
}
