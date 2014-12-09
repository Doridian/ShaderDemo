package de.doridian.crtdemo.simfs;


public abstract class AbstractData {
    Cluster attributeCluster;
    Cluster firstDataCluster;
    public String name;

    protected boolean dataDirty;
    protected boolean attributesDirty;

    protected abstract void setContents(byte[] content);
    protected abstract byte[] getContents();

    protected int getSetAttributes() {
        return 0;
    }
}
