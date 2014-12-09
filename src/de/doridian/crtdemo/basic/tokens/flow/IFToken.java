package de.doridian.crtdemo.basic.tokens.flow;

import de.doridian.crtdemo.basic.parameters.AbstractParameter;
import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "IF", noGroups = true)
public class IFToken extends AbstractToken {
    @Override
    public void insert() {
        String conditionExpression = "";
        String ifExpression = "";
        String elseExpression = "";
        int stage = 0;
        int prevStage = 0;

        AbstractParameter[] parameters = parametersSplitDetailed[0].subParams;

        for(int i = 0; i < parameters.length; i++) {
            AbstractParameter parameter = parameters[i];
            switch (stage) {
                case 0:
                    if (parameter.valueEquals("THEN")) {
                        stage = 1;
                        conditionExpression = getAsConditionalParameters(parameters, prevStage, i, false);
                        prevStage = i + 1;
                    }
                    break;
                case 1:
                    if(parameter.valueEquals("ELSE")) {
                        ifExpression = getAsAssignmentParameters(parameters, prevStage, i);
                        prevStage = i + 1;
                        i = parametersSplitDetailed.length;
                        stage = 2;
                    }
                    break;
            }
        }

        switch (stage) {
            case 0:
                throw new SyntaxException("INVALID IF CONSTRUCT");
            case 1:
                ifExpression = getAsAssignmentParameters(parameters, prevStage, parameters.length);
                break;
            case 2:
                elseExpression = getAsAssignmentParameters(parameters, prevStage, parameters.length);
                break;
        }

        ifExpression = "\t\tif(" + conditionExpression.trim() + ") {\n" + parseLine(ifExpression.trim()).getCode("\t\t\t") + "\n\t\t}";
        if(!elseExpression.isEmpty())
            elseExpression = " else {\n" + parseLine(elseExpression.trim()).getCode("\t\t\t") + "\n\t\t}";

        addLine(ifExpression + elseExpression);
    }

    @Override
    public String getCode(String prefix) {
        return null;
    }
}
