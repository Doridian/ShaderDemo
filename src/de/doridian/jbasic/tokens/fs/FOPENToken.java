package de.doridian.jbasic.tokens.fs;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("FOPEN")
public class FOPENToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        //FOPEN HANDLE1, "C:\\meow.txt"
        String fileHandle = parametersSplitDetailed[0].getAsParameter();
        String fileName = parametersSplitDetailed[1].getAsParameter();
        return prefix + fileHandle + " = $FS_FOPEN(" + fileName + ");";
    }
}
