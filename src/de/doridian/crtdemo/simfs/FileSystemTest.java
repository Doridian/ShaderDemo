package de.doridian.crtdemo.simfs;

import de.doridian.crtdemo.simfs.data.FileData;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileSystemTest {
    private static FileSystem fs;
    private static RandomAccessFile file = null;

    public static void main(String[] args) throws Exception {
        loadFS(true);
        doTest();

        loadFS(false);
        doTest();
    }

    public static void loadFS(boolean reset) throws IOException {
        if(file != null)
            file.close();

        file = new RandomAccessFile("test.fs", "rw");
        if(reset)
            fs = FileSystem.create(128, 32000, file);
        else
            fs = FileSystem.read(file);
    }

    private static void doTest() throws Exception {
        longFileTest();
        fs.rootDirectory.flush();
    }

    public static void longFileTest() throws Exception {
        FileData mainTxt = (FileData)fs.rootDirectory.findFile("test.bin");
        if(mainTxt == null) {
            mainTxt = new FileData(fs);
            mainTxt.setName("test.bin");

            mainTxt.seek(0);
            for(int i = 0; i < 255; i++) {
                mainTxt.writeByte(i);
                mainTxt.flush();
            }

            fs.rootDirectory.addFile(mainTxt);
        }

        //mainTxt.flush();

        mainTxt.seek(0);
        for(int i = 0; i < 255; i++) {
            int j = mainTxt.readUnsignedByte();
            if(j != i) {
                System.out.println("INVALID " + i + " != " + j);
                System.exit(0);
            }
        }
    }

    public static void basicFileTest() throws Exception {
        System.out.println(fs.rootDirectory.listFiles().length);
        for(int i = 0; i < 10; i++) {
            FileData mainTxt = (FileData)fs.rootDirectory.findFile("main_" + i + ".txt");

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
