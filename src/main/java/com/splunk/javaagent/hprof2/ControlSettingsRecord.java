package com.splunk.javaagent.hprof2;

/**
 * Created by fross on 1/9/14.
 */
public class ControlSettingsRecord extends HProfRecord {
    private boolean allocTracingOn;
    private boolean cpuSamplingOn;
    private short stackTraceDepth;

    public ControlSettingsRecord(HProfDumpFile parent, byte typeTag, long baseTimestamp, long timestampOffset,
                                 boolean allocTracingOn, boolean cpuSamplingOn, short stackTraceDepth) {
        super(parent, typeTag, baseTimestamp, timestampOffset);

        this.allocTracingOn = allocTracingOn;
        this.cpuSamplingOn = cpuSamplingOn;
        this.stackTraceDepth = stackTraceDepth;
    }

    public boolean isAllocTracingOn() {
        return allocTracingOn;
    }

    public boolean isCpuSamplingOn() {
        return cpuSamplingOn;
    }

    public short getStackTraceDepth() {
        return stackTraceDepth;
    }
}
