package de.doridian.jbasic.tokens.misc;

import de.doridian.jbasic.parameters.AbstractParameter;
import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("LET")
public class LETToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        AbstractParameter[] parameters = parametersSplitDetailed[0].subParams;

        String varName = (String)parameters[0].getValue();

        boolean isArray = varName.charAt(varName.length() - 1) == ']';

        if(parameters.length < 2) {
            if(isArray) {
                int openBrPos = varName.indexOf('[');
                int arraySize = Integer.parseInt(varName.substring(openBrPos + 1, varName.length() - 1));
                varName = varName.substring(0, openBrPos);
                program.addVariable(varName, true);

                return prefix + varName + " = new " + program.getVarType(varName) + "[" + arraySize + "];";
            } else {
                program.addVariable(varName, false);
                String varType = program.getVarType(varName);
                switch (varType) {
                    case "String":
                        return prefix + varName + " = \"\";";
                    case "int":
                        return prefix + varName + " = 0;";
                    default:
                        throw new SyntaxException("INVALID VAR TYPE");
                }
            }
        } else {
            if(!parameters[1].valueEquals("="))
                throw new SyntaxException();
            if(!isArray)
                program.addVariable(varName, false);
        }

        return prefix + varName + " = " + getAsAssignmentParameters(parameters, 2, parameters.length) + ";";
    }
}
