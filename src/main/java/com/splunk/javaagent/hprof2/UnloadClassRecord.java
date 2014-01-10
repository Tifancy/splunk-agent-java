package com.splunk.javaagent.hprof2;

import java.nio.MappedByteBuffer;

/**
 * Created by fross on 1/9/14.
 */
public class UnloadClassRecord extends HProfRecord {
    private long serialNumber;
    private String className;

    public UnloadClassRecord(HProfDumpFile parent, byte typeTag, long baseTimestamp,
                             long timestampOffset, long serialNumber, String className) {
        super(parent, typeTag, baseTimestamp, timestampOffset);

        this.serialNumber = serialNumber;
        this.className = className;
    }

    public long getSerialNumber() {
        return this.serialNumber;
    }

    public String getClassName() {
        return this.className;
    }
}
