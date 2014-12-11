package de.doridian.crtdemo.basic;

import de.doridian.crtdemo.Util;

import java.io.IOException;

public class RealFSBasicFS implements BasicFS {
    @Override
    public String getFileContents(String fileName) throws IOException {
        return Util.readFile("data/" + fileName);
    }
}
