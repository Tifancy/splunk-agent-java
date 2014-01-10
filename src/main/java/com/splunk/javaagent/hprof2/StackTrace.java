package com.splunk.javaagent.hprof2;

import java.util.List;

public class StackTrace {
    private long threadSerialNumber;
    private List<StackFrame> frames;

    public StackTrace(long threadSerialNumber, List<StackFrame> frames) {
        this.threadSerialNumber = threadSerialNumber;
        this.frames = frames;
    }

    public long getThreadSerialNumber() { return this.threadSerialNumber; }
    public List<StackFrame> getStackFrames() { return this.frames; }
}
