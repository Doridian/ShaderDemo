package de.doridian.crtdemo.basic.parameters;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

public class BracketedParameter extends AbstractParameter {
    private final GroupedParameter[] subParams;
    public final String preBracket;

    public BracketedParameter(String preBracket, String parameter) {
        super(AbstractToken.parseParameters(parameter, false));
        subParams = (GroupedParameter[])value;
        this.preBracket = preBracket;
    }

    @Override
    public String getAsParameter() {
        return preBracket + "(" + AbstractToken.getAsAssignmentParameters(subParams, 0, subParams.length) + ")";
    }
}
