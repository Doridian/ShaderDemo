package de.doridian.crtdemo.basic.tokens.misc;

import de.doridian.crtdemo.basic.parameters.AbstractParameter;
import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("LET")
public class LETToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        AbstractParameter[] parameters = parametersSplitDetailed[0].subParams;

        String varName = (String)parameters[0].getValue();

        boolean isArray = varName.charAt(varName.length() - 1) == ']';

        if(parameters.length < 2) { //ARRAY
            int openBrPos = varName.indexOf('[');
            int arraySize = Integer.parseInt(varName.substring(openBrPos + 1, varName.length() - 1));
            varName = varName.substring(0, openBrPos);
            program.addVariable(varName, arraySize);

            return prefix + varName + " = new " + program.getVarType(varName) + "[" + arraySize + "];";
        } else {
            if(!parameters[1].valueEquals("="))
                throw new SyntaxException();
            if(!isArray)
                program.addVariable(varName);
        }

        return prefix + getAsAssignmentParameters(0) + ";";
    }
}
