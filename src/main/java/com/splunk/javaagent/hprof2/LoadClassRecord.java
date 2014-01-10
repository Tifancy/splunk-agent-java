package com.splunk.javaagent.hprof2;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Map;

/**
 * Created by fross on 1/9/14.
 */
public class LoadClassRecord extends HProfRecord {
    private long serialNumber;
    private long objectId;
    private long stackTraceSerialNumber;
    private String className;

    public LoadClassRecord(HProfDumpFile parent, byte typeTag, long baseTimestamp, long timestampOffset,
                           long classSerialNumber, long classObjectId, long stackTraceSerialNumber, String className) {
        super(parent, typeTag, baseTimestamp, timestampOffset);

        this.serialNumber = classSerialNumber;
        this.stackTraceSerialNumber = stackTraceSerialNumber;
        this.objectId = classObjectId;
        this.className = className;
    }

    public long getSerialNumber() {
        return this.serialNumber;
    }

    public long getObjectId() {
        return this.objectId;
    }

    public long getStackTraceSerialNumber() {
        return this.stackTraceSerialNumber;
    }

    public String getClassName() {
        return this.className;
    }
}
