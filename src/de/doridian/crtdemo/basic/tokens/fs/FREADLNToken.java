package de.doridian.crtdemo.basic.tokens.fs;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("FREADLN")
public class FREADLNToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        //FREADLN HANDLE1, VAR
        String fileHandle = parametersSplitDetailed[0].getAsParameter();
        String variable = parametersSplitDetailed[1].getAsParameter();
        return prefix + variable + " = $FS_READLN(" + fileHandle + ");";
    }
}
