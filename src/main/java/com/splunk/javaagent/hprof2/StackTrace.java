package com.splunk.javaagent.hprof2;

import java.util.List;

public class StackTrace {
    private int threadSerialNumber;
    private List<StackFrame> frames;

    public StackTrace(int threadSerialNumber, List<StackFrame> frames) {
        this.threadSerialNumber = threadSerialNumber;
        this.frames = frames;
    }

    public int getThreadSerialNumber() { return this.threadSerialNumber; }
    public List<StackFrame> getStackFrames() { return this.frames; }
}
