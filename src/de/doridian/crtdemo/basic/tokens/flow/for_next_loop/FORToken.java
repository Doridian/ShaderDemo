package de.doridian.crtdemo.basic.tokens.flow.for_next_loop;

import de.doridian.crtdemo.basic.parameters.AbstractParameter;
import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "FOR", noGroups = true)
public class FORToken extends AbstractToken {
    @Override
    public void insert() {
        String variableName;
        int from, to, step = 1;

        AbstractParameter[] parameters = parametersSplitDetailed[0].subParams;

        if(!parameters[1].valueEquals("=") || !parameters[3].valueEquals("TO"))
            throw new SyntaxException();

        variableName = (String)parameters[0].getValue();
        from = (Integer)parameters[2].getValue();
        to = (Integer)parameters[4].getValue();
        if(parameters.length > 5) {
            if(!parameters[5].valueEquals("STEP"))
                throw new SyntaxException();
            step = (Integer)parameters[6].getValue();
        }

        if(step == 0)
            throw new SyntaxException();

        String comparator = ">=";
        if(step < 0)
            comparator = "<=";

        program.addVariable(variableName);
        addLine("\t\t" + variableName + " = " + from + ";");
        float lineCode = line + 0.1f;
        program.addLine(lineCode, "\t\tif(" + variableName + " " + comparator + " " + to + ") {\n\t\t\t$gotoAfter(" + endingToken.line + ");\n\t\t\treturn;\n\t\t}\n\t\t$addLoop(" + lineCode + "f, " + endingToken.line + ");\n\t\t" + variableName + " += " + step + ";");
    }

    public Class<? extends AbstractToken> getEndingToken() {
        return NEXTToken.class;
    }

    @Override
    public String getCode(String prefix) {
        return null;
    }
}
