package de.doridian.jbasic.parameters;

import de.doridian.jbasic.tokens.AbstractToken;

public class GroupedParameter extends AbstractParameter {
    public final AbstractParameter[] subParams;

    public GroupedParameter(AbstractParameter[] parameter) {
        super(parameter);
        subParams = (AbstractParameter[])value;
    }

    @Override
    public String getSeparator() {
        return ", ";
    }

    @Override
    public String getAsParameter() {
        return AbstractToken.getAsAssignmentParameters(subParams, 0, subParams.length);
    }
}
