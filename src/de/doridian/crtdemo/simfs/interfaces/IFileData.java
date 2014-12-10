package de.doridian.crtdemo.simfs.interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface IFileData extends IAbstractData, DataInput, DataOutput {
    public void writeEOF() throws IOException;
    public void setLength(int length) throws IOException;
    public void seek(int pos) throws IOException;
}
