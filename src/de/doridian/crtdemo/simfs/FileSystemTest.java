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
        System.out.println(fs.rootDirectory.listFiles().length);
        for(int i = 0; i < 10; i++) {
            FileData mainTxt = (FileData) fs.rootDirectory.findFile("main_" + i + ".txt");
            if (mainTxt == null) {
                mainTxt = new FileData(fs);
                mainTxt.setName("main_" + i + ".txt");
                mainTxt.flush();

                System.out.println("===" + i + "===");

                mainTxt.seek(3);
                mainTxt.writeShort(10);
                mainTxt.seek(3);
                if(mainTxt.readUnsignedShort() != 10)
                    System.out.println("INVALID");
                mainTxt.flush();
                mainTxt.seek(3);
                if(mainTxt.readUnsignedShort() != 10)
                    System.out.println("INVALID");

                fs.rootDirectory.addFile(mainTxt);
            } else {
                System.out.println("FOUND " + i);
            }
        }

        fs.rootDirectory.flush();
    }
}
