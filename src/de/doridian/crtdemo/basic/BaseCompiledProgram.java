package de.doridian.crtdemo.basic;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BaseCompiledProgram {
    protected Float $nextLinePointer = null;
    protected boolean $cleanExit = false;

    protected final float $entryPoint;

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

    protected void $addNoopLine(float line) {
        $lineNumbers.add(line);
        $lineMethods.put(line, null);
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
                io.print("\n---END OF PROGRAM REACHED WITHOUT END STATEMENT---\n");
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
        Method lineMethod = $lineMethods.get(runLine);
        if(lineMethod != null)
            lineMethod.invoke(this);
        return runLine;
    }
}
