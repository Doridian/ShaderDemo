package de.doridian.crtdemo.basic;

public interface BasicIO {
    public String readLine();
    public int readChar();

    public void clearLine(int line);
    public void clearScreen();

    public int getLines();
    public int getColumns();

    public void print(Object obj);
    public void print(Object obj, boolean invert);

    public void setCursor(int column, int line);
}
