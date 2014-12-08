package de.doridian.crtdemo.basic.tokens.flow.for_next_loop;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("FOR")
public class FORToken extends AbstractToken {
    @Override
    public void insert() {
        String variableName = "";
        int from = 0;
        int to = 0;
        int step = 1;

        if(!parametersSplitDetailed[1].valueEquals("=") || !parametersSplitDetailed[3].valueEquals("TO"))
            throw new SyntaxException();

        variableName = (String)parametersSplitDetailed[0].getValue();
        from = (Integer)parametersSplitDetailed[2].getValue();
        to = (Integer)parametersSplitDetailed[4].getValue();
        if(parametersSplitDetailed.length > 5) {
            if(!parametersSplitDetailed[5].valueEquals("STEP"))
                throw new SyntaxException();
            step = (Integer)parametersSplitDetailed[6].getValue();
        }

        if(step == 0)
            throw new SyntaxException();

        String comparator = ">=";
        if(step < 0)
            comparator = "<=";

        program.addVariable(variableName);
        addLine("\t\t" + variableName + " = " + from + ";");
        program.addLine(line + 0.1f, "\t\tif(" + variableName + " " + comparator + " " + to + ") {\n\t\t\t$gotoAfter(" + endingToken.line + ");\n\t\t\treturn;\n\t\t}\n\t\t$addLoop(" + line + ", " + endingToken.line + ");\n\t\t" + variableName + " += " + step + ";");
    }

    public Class<? extends AbstractToken> getEndingToken() {
        return NEXTToken.class;
    }

    @Override
    public String getCode(String prefix) {
        return null;
    }
}
