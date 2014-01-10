package com.splunk.javaagent.hprof2;

import java.util.Map;

/**
 * Created by fross on 1/10/14.
 */
public class CpuSamplesRecord extends HProfRecord {
    private final long totalSamples;
    private final Map<StackTrace, Long> samples;

    public CpuSamplesRecord(HProfDumpFile parent, byte typeTag, long baseTimestamp, long timestampOffset,
                            long totalSamples, Map<StackTrace, Long> samples) {
        super(parent, typeTag, baseTimestamp, timestampOffset);
        this.totalSamples = totalSamples;
        this.samples = samples;
    }

    public long getTotalSamples() {
        return totalSamples;
    }

    public Map<StackTrace, Long> getSamples() {
        return samples;
    }
}
