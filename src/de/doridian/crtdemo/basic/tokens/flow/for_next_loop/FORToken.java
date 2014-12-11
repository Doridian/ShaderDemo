package de.doridian.crtdemo.basic.tokens.flow.for_next_loop;

import de.doridian.crtdemo.basic.parameters.AbstractParameter;
import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "FOR", noGroups = true)
public class FORToken extends AbstractToken {
    @Override
    public void insert() {
        String variableName = "";
        String initialExpression = "";
        String toExpression = "";

        int step = 1;
        int stage = 0;
        int prevStage = 2;

        AbstractParameter[] parameters = parametersSplitDetailed[0].subParams;

        variableName = (String)parameters[0].getValue();

        if(!parameters[1].valueEquals("="))
            throw new SyntaxException();

        outer_loop:
        for(int i = 2; i < parameters.length; i++) {
            AbstractParameter parameter = parameters[i];
            switch (stage) {
                case 0:
                    if (parameter.valueEquals("TO")) {
                        stage = 1;
                        initialExpression = getAsAssignmentParameters(parameters, prevStage, i);
                        prevStage = i + 1;
                    }
                    break;
                case 1:
                    if(parameter.valueEquals("STEP")) {
                        stage = 2;
                        toExpression = getAsAssignmentParameters(parameters, prevStage, i);
                        prevStage = i + 1;
                        break outer_loop;
                    }
                    break;
            }
        }

        switch (stage) {
            case 0:
                throw new SyntaxException("INVALID IF CONSTRUCT");
            case 1:
                toExpression = getAsAssignmentParameters(parameters, prevStage, parameters.length);
                break;
            case 2:
                step = (Integer)parameters[prevStage].getValue();
                break;
        }

        if(step == 0)
            throw new SyntaxException();

        String comparator = ">=";
        if(step < 0)
            comparator = "<=";

        program.addVariable(variableName);
        addLine("\t\t" + variableName + " = " + initialExpression + ";");
        float lineCode = line + 0.1f;
        program.addLine(lineCode, "\t\tif(" + variableName + " " + comparator + " " + toExpression + ") {\n\t\t\t$gotoAfter(" + endingToken.line + ");\n\t\t\treturn;\n\t\t}\n\t\t$addLoop(" + lineCode + "f, " + endingToken.line + ");\n\t\t" + variableName + " += " + step + ";");
    }

    public Class<? extends AbstractToken> getEndingToken() {
        return NEXTToken.class;
    }

    @Override
    public String getCode(String prefix) {
        return null;
    }
}
