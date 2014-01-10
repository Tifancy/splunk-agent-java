package com.splunk.javaagent.hprof;

import com.splunk.javaagent.SplunkLogEvent;
import com.splunk.javaagent.hprof2.HProfDumpFile;

import java.nio.MappedByteBuffer;

public class LoadClassRecord extends HprofRecord {

	int classSerialNumber;
	HprofIDField classObjectID;
	int stackTraceSerial;
	HprofIDField classNameID;

    public LoadClassRecord(HProfDumpFile that, byte typeTag, long baseTimestamp, int timestampOffset, int nBytes, MappedByteBuffer buffer) {
        super();
    }

    @Override
	public void parseRecord() {

		this.classSerialNumber = buf.getInt();
		this.classObjectID = readId();
		this.stackTraceSerial = buf.getInt();
		this.classNameID = readId();

		parent.classNameMap.put(classObjectID, classNameID);

	}

	@Override
	public SplunkLogEvent getSplunkLogEvent() {
		SplunkLogEvent event = new SplunkLogEvent("hprof_loadclass",
				"splunkagent", false, false);
		addCommonSplunkLogEventFields(event);
		event.addPair("classSerial", this.classSerialNumber);
		event.addPair("classObjectID", this.classObjectID.toString());
		event.addPair("stackTraceSerial", this.stackTraceSerial);
		event.addPair("classNameID", this.classNameID.toString());

		return event;
	}

}
