package de.doridian.crtdemo.basic;

public interface BasicIO {
    public String readLine();
    public void clearLine(int line);
    public void clearScreen();
    public int getLines();
    public int getColumns();
    public void print(Object obj);
    public void setCursor(int column, int line);
}
