package de.doridian.jbasic.tokens.flow;

import de.doridian.jbasic.parameters.AbstractParameter;
import de.doridian.jbasic.tokens.AbstractToken;

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

        outer_loop:
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
                        stage = 2;
                        ifExpression = getAsAssignmentParameters(parameters, prevStage, i);
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
                ifExpression = getAsAssignmentParameters(parameters, prevStage, parameters.length);
                break;
            case 2:
                elseExpression = getAsAssignmentParameters(parameters, prevStage, parameters.length);
                break;
        }

        AbstractToken token = parseLine(ifExpression.trim());
        token.line = line;
        ifExpression = "\t\tif(" + conditionExpression.trim() + ") {\n" + token.getCode("\t\t\t") + "\n\t\t}";
        if(!elseExpression.isEmpty()) {
            token = parseLine(elseExpression.trim());
            token.line = line;
            elseExpression = " else {\n" + token.getCode("\t\t\t") + "\n\t\t}";
        }

        addLine(ifExpression + elseExpression);
    }

    @Override
    public String getCode(String prefix) {
        return null;
    }
}
