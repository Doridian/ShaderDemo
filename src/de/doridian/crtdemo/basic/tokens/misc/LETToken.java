package de.doridian.crtdemo.basic.tokens.misc;

import de.doridian.crtdemo.basic.parameters.AbstractParameter;
import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("LET")
public class LETToken extends AbstractToken {
    @Override
    public void insert() {
        AbstractParameter[] parameters = parametersSplitDetailed[0].subParams;
        if(!parameters[1].valueEquals("="))
            throw new SyntaxException();
        program.addVariable((String)parameters[0].getValue());
        super.insert();
    }

    @Override
    public String getCode(String prefix) {
        return prefix + getAsAssignmentParameters(0) + ";";
    }
}
