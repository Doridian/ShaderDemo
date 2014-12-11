package de.doridian.crtdemo.basic;

import java.io.IOException;

public interface BasicFS {
    public String getFileContents(String fileName) throws IOException;
}
