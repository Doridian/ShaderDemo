package de.doridian.jbasic.tokens.flow;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("BREAK")
public class BREAKToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        StringBuilder brk = new StringBuilder();

        if(parametersSplitDetailed.length > 0 && parametersSplitDetailed[0].subParams.length > 0) {
            Integer brkCount = (Integer)parametersSplitDetailed[0].subParams[0].getValue();
            if(brkCount == 0) {
                program.addNoopLine(line);
                return null;
            }

            if(brkCount > 1) {
                for(int i = 0; i < brkCount; i++) {
                    brk.append(prefix);
                    brk.append("$loopQueue.pop();\n");
                }
            }
        }
        brk.append(prefix);
        brk.append("$gotoAfter($loopQueue.pop().end);");
        return brk.toString();
    }
}
