package de.doridian.crtdemo.basic.tokens.fs;

import de.doridian.crtdemo.basic.tokens.AbstractToken;

@AbstractToken.TokenName("FWRITELN")
public class FWRITELNToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        //FWRITELN HANDLE1, DATA
        String fileHandle = parametersSplitDetailed[0].getAsParameter();
        String data = parametersSplitDetailed[1].getAsParameter();
        return prefix + "$FS_WRITELN(" + fileHandle + ", " + data + ");";
    }
}
