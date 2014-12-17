package de.doridian.crtdemo;

import de.doridian.crtdemo.simfs.FileSystem;
import de.doridian.crtdemo.simfs.interfaces.IDirectoryData;
import de.doridian.crtdemo.simfs.interfaces.IFileData;
import de.doridian.crtdemo.simfs.interfaces.IFileSystem;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FSTransferAgent {
    private static void transferFile(IDirectoryData dir, File src) throws IOException {
        IFileData file = dir.createFile(src.getName());
        file.write(Util.readFile(src).getBytes("ASCII"));
    }

    private static void transferFolder(IDirectoryData dir, File folder) throws IOException {
        for(File subFolder : folder.listFiles()) {
            if(subFolder.getName().charAt(0) == '.')
                continue;
            if(subFolder.isDirectory()) {
                IDirectoryData subDir = dir.createDirectory(subFolder.getName());
                transferFolder(subDir, subFolder);
            } else
                transferFile(dir, subFolder);
        }
    }

    public static void initAllFS() throws IOException {
        File baseFolder = new File("data/fssrc");
        for(File subFolder : baseFolder.listFiles()) {
            if(subFolder.getName().charAt(0) == '.')
                continue;
            if(subFolder.isDirectory()) {
                IFileSystem fs = initFS(subFolder);
                transferFolder(fs.getRootDirectory(), subFolder);
            }
        }
    }

    private static IFileSystem initFS(File folder) throws IOException {
        String[] metaArray = Util.readFile(new File(folder, ".FSMETA")).split("[\r\n]+");
        int clusterCount = -1;
        int clusterSize = -1;
        for(String meta : metaArray) {
            if(meta.isEmpty())
                continue;
            int colonIdx = meta.indexOf(':');
            if(colonIdx < 0)
                throw new RuntimeException("Invalid line in FS.META");
            String mValue = meta.substring(colonIdx + 1).trim();
            switch (meta.substring(0, colonIdx).trim().toUpperCase()) {
                case "CLUSTERSIZE":
                    if(clusterSize != -1)
                        throw new RuntimeException("Invalid line in FS.META");
                    clusterSize = Integer.parseInt(mValue);
                    if(clusterSize == -1)
                        throw new RuntimeException("Invalid line in FS.META");
                    break;
                case "CLUSTERCOUNT":
                    if(clusterCount != -1)
                        throw new RuntimeException("Invalid line in FS.META");
                    clusterCount = Integer.parseInt(mValue);
                    if(clusterCount == -1)
                        throw new RuntimeException("Invalid line in FS.META");
                    break;
                default:
                    throw new RuntimeException("Invalid line in FS.META");
            }
        }
        File fsFile = new File("data/filesystem/" + folder.getName());
        RandomAccessFile ranAF = new RandomAccessFile(fsFile, "rw");
        ranAF.setLength(0);
        return FileSystem.create(clusterSize, clusterCount, ranAF);
    }
}
