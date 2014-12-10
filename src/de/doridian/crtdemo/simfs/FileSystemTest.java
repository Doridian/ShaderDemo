package de.doridian.crtdemo.simfs;

import de.doridian.crtdemo.simfs.interfaces.IDirectoryData;
import de.doridian.crtdemo.simfs.interfaces.IFileData;
import de.doridian.crtdemo.simfs.interfaces.IFileSystem;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileSystemTest {
    private static IFileSystem fs;
    private static IDirectoryData rootDirectory;
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
            fs = FileSystem.create(512, 32000, file);
        else
            fs = FileSystem.read(file);
        rootDirectory = fs.getRootDirectory();
    }

    private static void doTest() throws Exception {
        longFileTest();
        //rootDirectory.flush();
    }

    public static void longFileTest() throws Exception {
        IFileData mainTxt = (IFileData)rootDirectory.findFile("test.bin");
        if(mainTxt == null) {
            mainTxt = rootDirectory.createFile("test.bin");

            mainTxt.seek(0);
            for(int i = 0; i < 1024; i++) {
                mainTxt.writeInt(i);
            }
        }

        //mainTxt.flush();

        mainTxt.seek(0);
        for(int i = 0; i < 1024; i++) {
            int j = mainTxt.readInt();
            if(j != i) {
                System.out.println("INVALID " + i + " != " + j);
                System.exit(0);
            }
        }
    }

    public static void basicFileTest() throws Exception {
        System.out.println(rootDirectory.listFiles().length);
        for(int i = 0; i < 10; i++) {
            IFileData mainTxt = (IFileData)rootDirectory.findFile("main_" + i + ".txt");

            if (mainTxt == null) {
                mainTxt = rootDirectory.createFile("main_" + i + ".txt");
                //mainTxt.flush();

                mainTxt.seek(0);
                mainTxt.writeShort(5);
                mainTxt.seek(2);
                mainTxt.writeShort(6);

                //mainTxt.flush();

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

        //rootDirectory.flush();
    }
}
