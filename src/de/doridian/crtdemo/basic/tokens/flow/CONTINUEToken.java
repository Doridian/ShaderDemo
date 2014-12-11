package de.doridian.crtdemo.basic.tokens.flow;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("CONTINUE")
public class CONTINUEToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        StringBuilder cnt = new StringBuilder();
        
        if(parametersSplitDetailed.length > 0 && parametersSplitDetailed[0].subParams.length > 0) {
            Integer cntCount = (Integer)parametersSplitDetailed[0].subParams[0].getValue();
            if(cntCount == 0) {
                program.addNoopLine(line);
                return null;
            }

            if(cntCount > 1) {
                for(int i = 0; i < cntCount; i++) {
                    cnt.append(prefix);
                    cnt.append("$loopQueue.poll();\n");
                }
            }
        }
        cnt.append(prefix);
        cnt.append("$goto($loopQueue.poll().start);");
        return cnt.toString();
    }
}
