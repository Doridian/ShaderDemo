package de.doridian.jsimfs.data;

import de.doridian.jsimfs.AbstractData;
import de.doridian.jsimfs.FileSystem;
import de.doridian.jsimfs.interfaces.IFileData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FileData extends AbstractData implements IFileData {
    public FileData(FileSystem fileSystem) {
        super(fileSystem);
    }

    @Override
    public void setAutoFlush(boolean autoFlush) {
        super.setAutoFlush(autoFlush);
        this.directAutoFlush = autoFlush;
    }

    @Override
    public byte[] readFully() throws IOException {
        seek(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int readLen;
        while((readLen = read(buffer, 0, buffer.length)) > 0)
            baos.write(buffer, 0, readLen);
        return baos.toByteArray();
    }
}
