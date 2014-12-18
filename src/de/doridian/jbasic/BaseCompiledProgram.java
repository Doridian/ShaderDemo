package de.doridian.jbasic;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BaseCompiledProgram {
    private Double $nextLinePointer = null;
    protected boolean $cleanExit = false;

    protected final double $entryPoint;

    protected final TreeSet<Double> $lineNumbers;
    protected final HashMap<Double, Method> $lineMethods;

    protected BasicIO $io;
    BasicFS $fs;

    boolean $debug;

    String $sourceCode;

    public String $getSourceCode() {
        return $sourceCode;
    }

    public static class LoopLines {
        public final double start;
        public final double end;

        public LoopLines(double start, double end) {
            this.start = start;
            this.end = end;
        }
    }

    public int SCREENWIDTH() {
        return $io.getColumns();
    }

    public int SCREENHEIGHT() {
        return $io.getLines();
    }

    private ArrayList<BasicFS.BasicFSFile> openFiles = new ArrayList<>();

    public int $FS_FOPEN(String fileName) throws IOException {
        synchronized (openFiles) {
            openFiles.add($fs.openFile(fileName));
            return openFiles.size() - 1;
        }
    }

    public void $FS_FCLOSE(int hdl) throws IOException {
        BasicFS.BasicFSFile file;
        synchronized (openFiles) {
            file = openFiles.remove(hdl);
        }
        file.close();
    }

    public String $FS_READLN(int hdl) throws IOException {
        BasicFS.BasicFSFile file;
        synchronized (openFiles) {
            file = openFiles.get(hdl);
        }
        return file.readLine();
    }

    public void $FS_WRITELN(int hdl, String str) throws IOException {
        BasicFS.BasicFSFile file;
        synchronized (openFiles) {
            file = openFiles.get(hdl);
        }
        file.writeLine(str);
    }

    protected ConcurrentLinkedDeque<Integer> $callQueue = null;
    protected ConcurrentLinkedDeque<LoopLines> $loopQueue = null;

    protected BaseCompiledProgram(Class<? extends BaseCompiledProgram> clazz, double entryPoint) {
        $entryPoint = entryPoint;
        $lineNumbers = new TreeSet<>();
        $lineMethods = new HashMap<>();

        for(Method m : clazz.getDeclaredMethods()) {
            String mName = m.getName();
            if(mName.startsWith("$LINE_")) {
                double lineNumber = Double.parseDouble(mName.substring(6).replace('_', '.'));
                $lineNumbers.add(lineNumber);
                $lineMethods.put(lineNumber, m);
            }
        }
    }

    public synchronized void $start(BasicIO io) {
        this.$io = io;
        double line = 0;

        this.$callQueue = new ConcurrentLinkedDeque<>();
        this.$loopQueue = new ConcurrentLinkedDeque<>();

        $nextLinePointer = $entryPoint;

        try {
            while ((line = $runNextLine()) >= 0) {
                System.out.println("L " + line);
            }
            if(!$cleanExit)
                io.print("\n--- PROGRAM REACHED EOF ---\n");
            else
                io.print("\n--- PROGRAM TERMINATED ---\n");
        } catch (Exception e) {
            e.printStackTrace();
            io.print("\nERROR ON LINE " + line + ": " + e.getMessage() + "\n");
        }

        for(BasicFS.BasicFSFile file : openFiles) {
            try { file.close(); } catch (Exception e) { }
        }
        openFiles.clear();
    }

    protected void $execSubFile(String file) throws IOException {
        new CodeParser($fs, file, $debug).compile().$start($io);
    }

    protected void $addLoop(double start, double end) {
        $loopQueue.push(new LoopLines(start, end));
    }

    protected void $goto(Double line) {
        if(line == null)
            $nextLinePointer = null;
        else
            $goto(line);
    }

    protected void $goto(double line) {
        $nextLinePointer = $lineNumbers.ceiling(line);
    }

    protected void $gotoAfter(double line) {
        $nextLinePointer = $lineNumbers.higher(line);
    }

    private Double $runNextLine() throws Exception {
        Double runLine = $nextLinePointer;
        if(runLine == null)
            return -1.0;
        $nextLinePointer = $lineNumbers.higher($nextLinePointer);
        Method lineMethod = $lineMethods.get(runLine);
        if(lineMethod != null)
            lineMethod.invoke(this);
        return runLine;
    }
}
