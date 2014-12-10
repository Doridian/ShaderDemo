package de.doridian.crtdemo.simfs.interfaces;

import java.io.Closeable;
import java.io.IOException;

public interface IAbstractData extends Closeable {
    public void flush() throws IOException;
    public void moveTo(IDirectoryData directoryData) throws IOException;
    public void setAutoFlush(boolean autoFlush);

    public String getName();
    public void setName(String name);

    public String getAbsolutePath();

    public IDirectoryData getParent() throws IOException;
}
