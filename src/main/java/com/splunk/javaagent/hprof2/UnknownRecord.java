package com.splunk.javaagent.hprof2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by fross on 1/9/14.
 */
public class UnknownRecord extends HProfRecord {
    private ArrayList<Byte> body;

    public UnknownRecord(HProfDumpFile parent, byte typeTag, long baseTimestamp,
                         long timestampOffset, ArrayList<Byte> body) {
        super(parent, typeTag, baseTimestamp, timestampOffset);

        this.body = body;
    }

    public ArrayList<Byte> getBody() { return body; }
}
