package com.splunk.javaagent.hprof2;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by fross on 1/9/14.
 */
public class UnknownRecord extends HProfRecord {
    private byte[] body;

    public UnknownRecord(HProfDumpFile parent, byte typeTag, long baseTimestamp,
                         int timestampOffset, byte[] body) {
        super(parent, typeTag, baseTimestamp, timestampOffset);

        this.body = body;
    }

    public byte[] getBody() { return body; }
}
