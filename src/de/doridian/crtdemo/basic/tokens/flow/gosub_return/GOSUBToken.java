package de.doridian.crtdemo.basic.tokens.flow.gosub_return;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("GOSUB")
public class GOSUBToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        return prefix + "$goto(" + getAsAssignmentParameters(0) + ");\n" + prefix + "$callQueue.add(" + line + ");";
    }
}
