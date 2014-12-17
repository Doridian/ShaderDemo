package de.doridian.crtdemo;

import de.doridian.jsimfs.FileSystem;
import de.doridian.jsimfs.interfaces.IDirectoryData;
import de.doridian.jsimfs.interfaces.IFileData;
import de.doridian.jsimfs.interfaces.IFileSystem;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FSTransferAgent {
    private static void transferFile(IDirectoryData dir, File src) throws IOException {
        IFileData file = dir.createFile(src.getName());
        file.write(Util.readFile(src).getBytes("ASCII"));
    }

    private static boolean ignoreFile(File file) {
        switch (file.getName()) {
            case ".":
            case "..":
                return true;
        }
        return false;
    }

    private static void transferFolder(IDirectoryData dir, File folder) throws IOException {
        for(File subFolder : folder.listFiles()) {
            if(ignoreFile(subFolder))
                continue;
            if(subFolder.isDirectory())
                transferFolder(dir.createDirectory(subFolder.getName()), subFolder);
            else
                transferFile(dir, subFolder);
        }
    }

    private static class FSMetaFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".fsmeta");
        }
    }

    public static void initAllFS() throws IOException {
        File baseFolder = new File("data/fssrc");
        for(File subFolder : baseFolder.listFiles(new FSMetaFilenameFilter())) {
            if(ignoreFile(subFolder) || subFolder.isDirectory())
                continue;
            initFS(subFolder);
        }
    }

    private static void initFS(File fsMeta) throws IOException {
        String[] metaArray = Util.readFile(fsMeta).split("[\r\n]+");
        String fsName = fsMeta.getName().substring(0, fsMeta.getName().lastIndexOf('.'));
        int clusterCount = -1;
        int clusterSize = -1;
        for(String meta : metaArray) {
            if(meta.isEmpty())
                continue;
            int colonIdx = meta.indexOf(':');
            if(colonIdx < 0)
                throw new RuntimeException("Invalid line in FS META");
            String mValue = meta.substring(colonIdx + 1).trim();
            switch (meta.substring(0, colonIdx).trim().toUpperCase()) {
                case "CLUSTERSIZE":
                    if(clusterSize != -1)
                        throw new RuntimeException("Invalid line in FS META");
                    clusterSize = Integer.parseInt(mValue);
                    if(clusterSize == -1)
                        throw new RuntimeException("Invalid line in FS META");
                    break;
                case "CLUSTERCOUNT":
                    if(clusterCount != -1)
                        throw new RuntimeException("Invalid line in FS META");
                    clusterCount = Integer.parseInt(mValue);
                    if(clusterCount == -1)
                        throw new RuntimeException("Invalid line in FS META");
                    break;
                default:
                    throw new RuntimeException("Invalid line in FS META");
            }
        }
        File fsFile = new File("data/filesystem/" + fsName + ".fs");
        RandomAccessFile ranAF = new RandomAccessFile(fsFile, "rw");
        ranAF.setLength(0);
        IFileSystem fs = FileSystem.create(clusterSize, clusterCount, ranAF);
        transferFolder(fs.getRootDirectory(), new File(fsMeta.getParentFile(), fsName));
    }
}
