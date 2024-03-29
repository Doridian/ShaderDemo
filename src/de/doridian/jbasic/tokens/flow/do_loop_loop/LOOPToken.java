package de.doridian.jbasic.tokens.flow.do_loop_loop;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName(value = "LOOP", noGroups = true)
public class LOOPToken extends AbstractToken {
    @Override
    public void insert() {
        boolean negate = false;
        if(parametersSplitDetailed.length > 0) {
            if (parametersSplitDetailed[0].valueEquals("WHILE"))
                negate = true;
            else if(!parametersSplitDetailed[0].valueEquals("UNTIL"))
                throw new SyntaxException("NOT WHILE NOR UNTIL");
            addLine("\t\tif(" + getAsConditionalParameters(1, negate) + ")\n\t\t\t$goto($loopQueue.pop().start);");
        } else {
            addLine("\t\t$goto($loopQueue.pop().start);");
        }
    }

    @Override
    public String getCode(String prefix) {
        return null;
    }
}
