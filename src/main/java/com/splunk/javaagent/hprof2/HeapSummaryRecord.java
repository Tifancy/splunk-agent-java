package com.splunk.javaagent.hprof2;

/**
 * Created by fross on 1/9/14.
 */
public class HeapSummaryRecord extends HProfRecord {
    private final int nLiveBytes;
    private final int nLiveInstances;
    private final long nTotalBytes;
    private final long nTotalInstances;

    public HeapSummaryRecord(HProfDumpFile parent, byte typeTag, long baseTimestamp, int timestampOffset,
                             int nLiveBytes, int nLiveInstances, long nTotalBytes, long nTotalInstances) {
        super(parent, typeTag, baseTimestamp, timestampOffset);

        this.nLiveBytes = nLiveBytes;
        this.nLiveInstances = nLiveInstances;
        this.nTotalBytes = nTotalBytes;
        this.nTotalInstances = nTotalInstances;
    }

    public int getLiveBytes() {
        return nLiveBytes;
    }

    public int getNumberOfLiveInstances() {
        return nLiveInstances;
    }

    public long getTotalBytes() {
        return nTotalBytes;
    }

    public long getTotalNumberOfInstances() {
        return nTotalInstances;
    }
}
