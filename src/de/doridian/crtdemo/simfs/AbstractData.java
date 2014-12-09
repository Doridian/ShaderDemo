package de.doridian.crtdemo.simfs;


import java.util.ArrayList;

public abstract class AbstractData {
    Cluster attributeCluster;
    Cluster firstDataCluster;
    public String name;

    protected ArrayList<Integer> dataClustersDirty;
    protected boolean allDataDirty;
    protected boolean attributesDirty;

    protected abstract void setContents(byte[] content);
    protected abstract byte[] getContents();

    protected int getSetAttributes() {
        return 0;
    }
}
