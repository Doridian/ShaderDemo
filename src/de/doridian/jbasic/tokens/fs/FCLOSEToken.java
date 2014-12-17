package de.doridian.jbasic.tokens.fs;

import de.doridian.jbasic.tokens.AbstractToken;

@AbstractToken.TokenName("FCLOSE")
public class FCLOSEToken extends AbstractToken {
    @Override
    public String getCode(String prefix) {
        //FCLOSE HANDLE1
        String fileHandle = parametersSplitDetailed[0].getAsParameter();
        return prefix + "$FS_FCLOSE(" + fileHandle + ");";
    }
}
