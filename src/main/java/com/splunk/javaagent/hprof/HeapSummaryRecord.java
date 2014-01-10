package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;
import com.splunk.javaagent.hprof2.HProfDumpFile;

public class HeapSummaryRecord extends HprofRecord {

	private int totalLiveBytes;
	private int totalLiveInstances;
	private long totalBytesAllocated;
	private long totalInstancesAllocated;

    public HeapSummaryRecord(HProfDumpFile that, byte typeTag, long baseTimestamp, int timestampOffset, int nLiveBytes, int nLiveInstances, long nTotalBytes, long nTotalInstances) {

    }

    @Override
	public void parseRecord() {

		this.totalLiveBytes = buf.getInt();
		this.totalLiveInstances = buf.getInt();
		this.totalBytesAllocated = buf.getLong();
		this.totalInstancesAllocated = buf.getLong();

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_heapsummary",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("totalLiveBytes", this.totalLiveBytes);
		event.addPair("totalLiveInstances", this.totalLiveInstances);
		event.addPair("totalBytesAllocated", this.totalBytesAllocated);
		event.addPair("totalInstancesAllocated", this.totalInstancesAllocated);

		return event;
	}

}
