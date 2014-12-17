package de.doridian.jsimfs;

import de.doridian.jsimfs.interfaces.IDirectoryData;
import de.doridian.jsimfs.interfaces.IFileData;
import de.doridian.jsimfs.interfaces.IFileSystem;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileSystemTest {
    private static IFileSystem fs;
    private static IDirectoryData rootDirectory;
    private static RandomAccessFile file = null;

    public static void main(String[] args) throws Exception {
        loadFS(true);
        doTest(true);

        loadFS(false);
        doTest(false);

        System.out.println("================");
        System.out.println("ALL TESTS PASSED");
        System.out.println("================");
    }

    public static void loadFS(boolean reset) throws IOException {
        if(file != null)
            file.close();

        file = new RandomAccessFile("data/tmp/test.fs", "rw");
        if(reset)
            fs = FileSystem.create(512, 32000, file);
        else
            fs = FileSystem.read(file);
        rootDirectory = fs.getRootDirectory();
    }

    private static void doTest(boolean mayCreate) throws Exception {
        deepFileTest(mayCreate);
        longFileTest(mayCreate);
        //rootDirectory.flush();
    }

    public static void longFileTest(boolean mayCreate) throws Exception {
        IFileData mainTxt = (IFileData)rootDirectory.findFile("test.bin");
        if(mainTxt == null) {
            if(!mayCreate)
                throw new Exception("INVALID0");

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

    public static void deepFileTest(boolean mayCreate) throws Exception {
        IDirectoryData directory = rootDirectory;
        for(int i = 0; i < 10; i++) {
            IDirectoryData subDirectory = (IDirectoryData)directory.findFile("d" + i);
            if(subDirectory == null) {
                if(!mayCreate)
                    throw new Exception("INVALID0");
                subDirectory = directory.createDirectory("d" + i);
                System.out.println("D CREATE " + i);
            } else {
                System.out.println("D FOUND " + i);
            }

            basicFileTest(subDirectory, mayCreate);

            directory = subDirectory;
        }
    }

    public static void basicFileTest(IDirectoryData directoryData, boolean mayCreate) throws Exception {
        //System.out.println(directoryData.listFiles().length);
        for(int i = 0; i < 10; i++) {
            IFileData mainTxt = (IFileData)directoryData.findFile("main_" + i + ".txt");

            if (mainTxt == null) {
                if(!mayCreate)
                    throw new Exception("INVALID0");
                mainTxt = directoryData.createFile("main_" + i + ".txt");
                //mainTxt.flush();

                mainTxt.seek(0);
                mainTxt.writeShort(5);
                mainTxt.seek(2);
                mainTxt.writeShort(6);

                //mainTxt.flush();

                System.out.println("F CREATE " + i);
            } else {
                System.out.println("F FOUND " + i);
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
