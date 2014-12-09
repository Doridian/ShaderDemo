package de.doridian.crtdemo.simfs.data;

import de.doridian.crtdemo.simfs.AbstractData;

public class FileData extends AbstractData {
    @Override
    protected void setContents(byte[] content) {

    }

    @Override
    protected byte[] getContents() {
        return new byte[0];
    }
}
