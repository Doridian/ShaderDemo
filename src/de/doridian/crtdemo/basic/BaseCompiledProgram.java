package de.doridian.crtdemo.basic;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BaseCompiledProgram {
    protected Float $nextLinePointer = null;
    protected boolean $cleanExit = false;

    protected final int $entryPoint;

    protected final TreeSet<Float> $lineNumbers;
    protected final HashMap<Float, Method> $lineMethods;

    protected BasicIO $io;

    String $sourceCode;

    public String $getSourceCode() {
        return $sourceCode;
    }

    public static class LoopLines {
        public final int start;
        public final int end;

        public LoopLines(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    protected ConcurrentLinkedQueue<Integer> $callQueue = null;
    protected ConcurrentLinkedQueue<LoopLines> $loopQueue = null;

    //private final Class<? extends BaseCompiledProgram> $clazz;

    protected BaseCompiledProgram(Class<? extends BaseCompiledProgram> clazz, int entryPoint) {
        $entryPoint = entryPoint;
        //$clazz = clazz;
        $lineNumbers = new TreeSet<Float>();
        $lineMethods = new HashMap<Float, Method>();

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

        this.$callQueue = new ConcurrentLinkedQueue<Integer>();
        this.$loopQueue = new ConcurrentLinkedQueue<LoopLines>();

        $nextLinePointer = (float)$entryPoint;
        try {
            while ((line = $runNextLine()) >= 0);
            if(!$cleanExit)
                io.print("\nEND OF PROGRAM REACHED WITHOUT END STATEMENT\n");
        } catch (Exception e) {
            e.printStackTrace();
            io.print("\nERROR ON LINE " + line + ": " + e.getMessage() + "\n");
        }
    }

    protected void $addLoop(int start, int end) {
        $loopQueue.add(new LoopLines(start, end));
    }

    protected void $goto(float line) {
        $nextLinePointer = line;
    }

    protected void $gotoAfter(float line) {
        $goto($lineNumbers.higher(line));
    }

    private Float $runNextLine() throws Exception {
        Float runLine = $nextLinePointer;
        if(runLine == null)
            return -1.0f;
        $nextLinePointer = $lineNumbers.higher($nextLinePointer);
        $lineMethods.get(runLine).invoke(this);
        return runLine;
    }
}
