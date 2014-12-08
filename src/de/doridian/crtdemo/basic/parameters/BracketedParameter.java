package de.doridian.crtdemo.basic.parameters;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

public class BracketedParameter extends AbstractParameter {
    private final AbstractParameter[] subParams;
    public final String preBracket;

    public BracketedParameter(String preChars, String preBracket, String parameter) {
        super(preChars, AbstractToken.parseParameters(parameter));
        subParams = (AbstractParameter[])value;
        this.preBracket = preBracket;
    }

    @Override
    public String getAsParameter() {
        return preBracket + "(" + AbstractToken.getAsAssignmentParameters(subParams, 0, subParams.length) + ")";
    }
}
