package de.doridian.crtdemo.basic;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BaseCompiledProgram {
    private Float $nextLinePointer = null;
    protected boolean $cleanExit = false;

    protected final float $entryPoint;

    protected final TreeSet<Float> $lineNumbers;
    protected final HashMap<Float, Method> $lineMethods;

    protected BasicIO $io;
    BasicFS $fs;

    boolean $debug;

    String $sourceCode;

    public String $getSourceCode() {
        return $sourceCode;
    }

    public static class LoopLines {
        public final float start;
        public final float end;

        public LoopLines(float start, float end) {
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

    protected ConcurrentLinkedQueue<Integer> $callQueue = null;
    protected ConcurrentLinkedQueue<LoopLines> $loopQueue = null;

    protected BaseCompiledProgram(Class<? extends BaseCompiledProgram> clazz, float entryPoint) {
        $entryPoint = entryPoint;
        $lineNumbers = new TreeSet<>();
        $lineMethods = new HashMap<>();

        for(Method m : clazz.getDeclaredMethods()) {
            String mName = m.getName();
            if(mName.startsWith("$LINE_")) {
                float lineNumber = Float.parseFloat(mName.substring(6).replace('_', '.'));
                $lineNumbers.add(lineNumber);
                $lineMethods.put(lineNumber, m);
            }
        }
    }

    public synchronized void $start(BasicIO io) {
        this.$io = io;
        float line = 0;

        this.$callQueue = new ConcurrentLinkedQueue<>();
        this.$loopQueue = new ConcurrentLinkedQueue<>();

        $nextLinePointer = $entryPoint;
        try {
            while ((line = $runNextLine()) >= 0);
            if(!$cleanExit)
                io.print("\n--- PROGRAM REACHED EOF ---\n");
            else
                io.print("\n--- PROGRAM TERMINATED ---\n");
        } catch (Exception e) {
            e.printStackTrace();
            io.print("\nERROR ON LINE " + line + ": " + e.getMessage() + "\n");
        }
    }

    protected void $execSubFile(String file) throws IOException {
        new CodeParser($fs, file, $debug).compile().$start($io);
    }

    protected void $addLoop(float start, float end) {
        $loopQueue.add(new LoopLines(start, end));
    }

    protected void $goto(Float line) {
        if(line == null)
            $nextLinePointer = null;
        else
            $goto((float)line);
    }

    protected void $goto(float line) {
        $nextLinePointer = $lineNumbers.ceiling(line);
    }

    protected void $gotoAfter(float line) {
        $nextLinePointer = $lineNumbers.higher(line);
    }

    private Float $runNextLine() throws Exception {
        Float runLine = $nextLinePointer;
        if(runLine == null)
            return -1.0f;
        $nextLinePointer = $lineNumbers.higher($nextLinePointer);
        Method lineMethod = $lineMethods.get(runLine);
        if(lineMethod != null)
            lineMethod.invoke(this);
        return runLine;
    }
}
