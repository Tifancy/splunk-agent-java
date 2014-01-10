package com.splunk.javaagent.hprof2;

import java.nio.MappedByteBuffer;

/**
 * Created by fross on 1/9/14.
 */
public class UnloadClassRecord extends HProfRecord {
    private int serialNumber;
    private String className;

    public UnloadClassRecord(HProfDumpFile parent, byte typeTag, long baseTimestamp,
                             int timestampOffset, int serialNumber, String className) {
        super(parent, typeTag, baseTimestamp, timestampOffset);

        this.serialNumber = serialNumber;
        this.className = className;
    }

    public int getSerialNumber() {
        return this.serialNumber;
    }

    public String getClassName() {
        return this.className;
    }
}
