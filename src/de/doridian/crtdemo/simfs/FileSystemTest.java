package de.doridian.crtdemo.simfs;

import de.doridian.crtdemo.simfs.data.FileData;

import java.io.RandomAccessFile;

public class FileSystemTest {
    public static void main(String[] args) throws Exception {
        final RandomAccessFile file = new RandomAccessFile("test.fs", "rw");
        final FileSystem fs;
        if(file.length() > 0)
            fs = FileSystem.read(file);
        else
            fs = FileSystem.create(128, 32000, file);
        FileData mainTxt = (FileData)fs.rootDirectory.findFile("main.txt");
        System.out.println(fs.rootDirectory.listFiles().length);
        if(mainTxt == null) {
            mainTxt = new FileData(fs);
            mainTxt.setName("main.txt");
            mainTxt.flush();
            fs.rootDirectory.addFile(mainTxt);
        } else {
            System.out.println("FOUND");
        }

        fs.rootDirectory.flush();
    }
}
