package de.doridian.crtdemo;

import de.doridian.crtdemo.simfs.FileSystem;
import de.doridian.crtdemo.simfs.interfaces.IFileData;
import de.doridian.crtdemo.simfs.interfaces.IFileSystem;

import java.io.RandomAccessFile;

public class FSTransferAgent {
    public static void transferFile(String src) {
        try {
            RandomAccessFile ranAF = new RandomAccessFile("data/filesystem/C", "rw");
            IFileSystem fs = FileSystem.read(ranAF);
            IFileData file = fs.getRootDirectory().createFile(src);
            file.write(Util.readFile("data/" + src).getBytes("ASCII"));
            ranAF.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void initFS() {
        try {
            RandomAccessFile ranAF = new RandomAccessFile("data/filesystem/C", "rw");
            ranAF.setLength(0);
            FileSystem.create(512, 32000, ranAF);
            ranAF.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
