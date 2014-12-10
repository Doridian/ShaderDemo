package de.doridian.crtdemo;

import de.doridian.crtdemo.simfs.FileSystem;
import de.doridian.crtdemo.simfs.interfaces.IAbstractData;
import de.doridian.crtdemo.simfs.interfaces.IFileSystem;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.TreeMap;

public class DriveGroup {
    public char currentDrive = 'C';
    public TreeMap<Character, IFileSystem> drives = new TreeMap<>();

    private final File folder;

    public DriveGroup(File folder) throws IOException {
        this.folder = folder;

        for(File file : folder.listFiles()) {
            if(file.getName().length() != 1)
                continue;
            drives.put(file.getName().charAt(0), FileSystem.read(new RandomAccessFile(file, "rw")));
        }
    }

    public void addFileSystem(char letter, int clusterSize, int numClusters) throws IOException {
        FileSystem.create(clusterSize, numClusters, new RandomAccessFile(new File(folder, ""+letter), "rw"));
    }

    public IFileSystem getDrive(char driveLetter) {
        return drives.get(driveLetter);
    }

    public TreeMap<Character, IFileSystem> getDrives() {
        return drives;
    }

    public IAbstractData getFile(String fileName) throws IOException {
        char useDrive = currentDrive;

        if(fileName.length() >= 3) {
            char driveLetter = fileName.substring(0, 1).toUpperCase().charAt(0);
            char colon = fileName.charAt(1);
            char pathStep = fileName.charAt(2);
            if(driveLetter >= 'A' && driveLetter <= 'Z' && colon == ':' && pathStep == FileSystem.PATH_SEPARATOR) {
                fileName = fileName.substring(2);
                useDrive = driveLetter;
            }
        }

        return getDrive(useDrive).getFile(fileName);
    }
}
