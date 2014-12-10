package de.doridian.crtdemo.simfs;

import de.doridian.crtdemo.simfs.data.FileData;

import java.io.RandomAccessFile;

public class FileSystemTest {
    public static void main(String[] args) throws Exception {
        final RandomAccessFile file = new RandomAccessFile("test.fs", "rw");
        final FileSystem fs;
        //file.setLength(0);
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
                //mainTxt.flush();

                mainTxt.seek(0);
                mainTxt.writeShort(5);
                mainTxt.seek(2);
                mainTxt.writeShort(6);

                mainTxt.flush();

                fs.rootDirectory.addFile(mainTxt);

                System.out.println("CREATE " + i);
            } else {
                System.out.println("FOUND " + i);
            }

            mainTxt.seek(0);
            if(mainTxt.readUnsignedShort() != 5)
                throw new RuntimeException("INVALID1");
            if(mainTxt.readUnsignedShort() != 6)
                throw new RuntimeException("INVALID2");
        }

        fs.rootDirectory.flush();
    }
}
