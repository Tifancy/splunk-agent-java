package com.splunk.javaagent.hprof2;

/*
 * Copyright 2014 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.*;

// TODO: Remove old hprof package and rename this one to hprof.
// TODO: Add formatting methods for logging to the HProfRecord subclasses.

public class HProfDumpFile implements Iterable<HProfRecord> {
    public static final byte RECORD_UTF8 = 1;
    public static final byte RECORD_LOAD_CLASS = 2;
    public static final byte RECORD_UNLOAD_CLASS = 3;
    public static final byte RECORD_STACK_FRAME = 4;
    public static final byte RECORD_STACK_TRACE = 5;
    public static final byte RECORD_ALLOC_SITES = 6;
    public static final byte RECORD_HEAP_SUMMARY = 7;
    public static final byte RECORD_START_THREAD = 10;
    public static final byte RECORD_END_THREAD = 11;
    public static final byte RECORD_CPU_SAMPLES = 13;
    public static final byte RECORD_CONTROL_SETTINGS = 14;
    public static final byte RECORD_HEAP_DUMP = 12;
    public static final byte RECORD_HEAP_DUMP_SEGMENT = 28;
    public static final byte RECORD_HEAP_DUMP_END = 44;

    public static final byte RECORD_GC_ROOT_UNKNOWN = -1;
    public static final byte RECORD_GC_ROOT_JNI_GLOBAL = 1;
    public static final byte RECORD_GC_ROOT_JNI_LOCAL = 2;
    public static final byte RECORD_GC_ROOT_JAVA_FRAME = 3;
    public static final byte RECORD_GC_ROOT_NATIVE_STACK = 4;
    public static final byte RECORD_GC_ROOT_STICKY_CLASS = 5;
    public static final byte RECORD_GC_ROOT_THREAD_BLOCK = 6;
    public static final byte RECORD_GC_ROOT_MONITOR_USED = 7;
    public static final byte RECORD_GC_ROOT_THREAD_OBJ = 8;
    public static final byte RECORD_GC_CLASS_DUMP = 32;
    public static final byte RECORD_GC_INSTANCE_DUMP = 33;
    public static final byte RECORD_GC_OBJ_ARRAY_DUMP = 34;
    public static final byte RECORD_GC_PRIM_ARRAY_DUMP = 35;

    public static final byte TYPE_ARRAY_OBJECT = 1;
    public static final byte TYPE_OBJECT = 2;
    public static final byte TYPE_BOOLEAN = 4;
    public static final byte TYPE_CHAR = 5;
    public static final byte TYPE_FLOAT = 6;
    public static final byte TYPE_DOUBLE = 7;
    public static final byte TYPE_BYTE = 8;
    public static final byte TYPE_SHORT = 9;
    public static final byte TYPE_INT = 10;
    public static final byte TYPE_LONG = 11;

    private MappedByteBuffer buffer;

    private String formatString;
    private int identifierSize;
    private long baseTimestamp;


    public HProfDumpFile(File file) throws IOException, ParseException {
        this.buffer = new RandomAccessFile(file, "r").getChannel().map(
                FileChannel.MapMode.READ_ONLY,
                0L,
                file.length()
        );

        byte[] formatStringBytes = new byte[18];
        this.buffer.get(formatStringBytes);
        this.formatString = new String(formatStringBytes, "UTF-8");
        buffer.get(); // Read the null byte terminating the string.

        if (!this.formatString.equals("JAVA PROFILE 1.0.1") &&
                !this.formatString.equals("JAVA PROFILE 1.0.2"))
            throw new ParseException("HPROF dump file did not begin with a valid format string (found: \"" +
                    this.formatString + "\", expected \"JAVA PROFILE 1.0.1\" or \"JAVA PROFILE 1.0.2\")",
                    0
            );

        this.identifierSize = buffer.getInt();
        if (identifierSize != 4 && identifierSize != 8)
            throw new ParseException("HPROF dump file did not specify a valid identifier size (found: " +
                    identifierSize + "; expected 4 or 8)", buffer.position() - 4);

        this.baseTimestamp = buffer.getLong();
    }

    public String getFormatString() {
        return this.formatString;
    }

    public int getIdentifierSize() {
        return this.identifierSize;
    }

    public long getBaseTimestamp() {
        return this.baseTimestamp;
    }

    // Package-private
    long getID(ByteBuffer buffer) {
        if (identifierSize == 4)
            return buffer.getInt();
        else if (identifierSize == 8)
            return buffer.getLong();
        else
            throw new RuntimeException("Precondition in this class that identifierSize " +
                    "== 4 or 8 has been violated.");
    }

    // Accumulators used to store records that are referred to by other records.
    private Map<Long, String> utf8Strings = new HashMap<Long, String>();
    private Map<Integer, String> classNames = new HashMap<Integer, String>();
    private Map<Long, StackFrame> stackFrames = new HashMap<Long, StackFrame>();
    private Map<Integer, StackTrace> stackTraces = new HashMap<Integer, StackTrace>();

    public Map<Long, String> getUtf8Strings() {
        return utf8Strings;
    }

    public Map<Integer, String> getClassNames() {
        return classNames;
    }

    public Map<Long, StackFrame> getStackFrames() {
        return stackFrames;
    }

    public Map<Integer, StackTrace> getStackTraces() {
        return stackTraces;
    }

    // TODO: Write JavaDoc for this.
    public Iterator<HProfRecord> iterator() {
        final HProfDumpFile that = this;
        return new Iterator<HProfRecord>() {

            @Override
            public boolean hasNext() {
                return buffer.position() < buffer.limit();
            }

            @Override
            public HProfRecord next() {
                // TODO: Explain the file format and give a link to its definition. What do the terms below mean?
                byte typeTag;
                int timestampOffset, nBytes;

                while (true) { // Iterate until we get a terminating record and return.
                    typeTag = buffer.get();
                    timestampOffset = buffer.getInt();
                    nBytes = buffer.getInt();

                    if (RECORD_UTF8 == typeTag) {
                        // Record format:
                        //
                        //     ID  -- ID used to reference this string.
                        //   [u1]* -- UTF8-encoded bytes for this string (which are NOT null terminated)

                        long stringId = that.getID(buffer);

                        byte[] stringBytes = new byte[nBytes - identifierSize];
                        buffer.get(stringBytes);
                        String string;
                        try {
                            string = new String(stringBytes, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException("Your JVM doesn't support UTF-8. This shouldn't happen...");
                        }

                        utf8Strings.put(stringId, string);

                    } else if (RECORD_STACK_FRAME == typeTag) {
                        // Record format:
                        //
                        //     ID -- ID used to reference this stack frame
                        //     ID -- method name string ID
                        //     ID -- method signature string ID
                        //     ID -- source file name string ID
                        //     int32 -- class serial number
                        //     int32 -- line number (if >0), or no information (<=0)

                        long stackFrameId = getID(buffer);
                        String methodName = utf8Strings.get(getID(buffer));
                        String methodSignature = utf8Strings.get(getID(buffer));
                        String sourceFileName = utf8Strings.get(getID(buffer));
                        int classSerialNumber = buffer.getInt();
                        String className = classNames.get(classSerialNumber);
                        int lineNumber = buffer.getInt();
                        StackFrame stackFrame = new StackFrame(
                                methodName, methodSignature, sourceFileName,
                                classSerialNumber, className,
                                lineNumber > 0 ? lineNumber : null
                        );
                        stackFrames.put(stackFrameId, stackFrame);

                    } else if (RECORD_STACK_TRACE == typeTag) {
                        // Record format:
                        //
                        //     int32 -- stack trace serial number
                        //     int32 -- thread serial number
                        //     int32 -- number of frames
                        //     [ID]* -- stack frame IDs

                        int stackTraceSerialNumber = buffer.getInt();
                        int threadSerialNumber = buffer.getInt();
                        int nFrames = buffer.getInt();
                        List<StackFrame> framesInTrace = new ArrayList<StackFrame>();
                        for (int i = 0; i < nFrames; i++) {
                            long stackFrameId = getID(buffer);
                            framesInTrace.add(stackFrames.get(stackFrameId));
                        }

                        StackTrace stackTrace = new StackTrace(threadSerialNumber, framesInTrace);
                        stackTraces.put(stackTraceSerialNumber, stackTrace);

                    } else if (RECORD_LOAD_CLASS == typeTag) {
                        // This is a terminating record, and should return a LoadClassRecord.
                        // Record format:
                        //
                        //     int32 -- class serial number (always >0)
                        //      ID   -- class object ID
                        //     int32 -- serial number referencing the stack trace this was loaded in
                        //      ID   -- String ID referencing the name of the class

                        int classSerialNumber = buffer.getInt();
                        long classObjectId = getID(buffer);
                        int stackTraceSerialNumber = buffer.getInt();
                        String className = utf8Strings.get(getID(buffer));

                        LoadClassRecord record = new LoadClassRecord(
                                that, typeTag, baseTimestamp,
                                timestampOffset, classSerialNumber, classObjectId,
                                stackTraceSerialNumber, className
                        );
                        // Store the class name so we can refer to it in UnloadClassRecord (since it is
                        // not included in that event.
                        classNames.put(record.getSerialNumber(), record.getClassName());
                        return record;

                    } else if (RECORD_UNLOAD_CLASS == typeTag) {
                        // This is a terminating record and should return an UnloadClassRecord.
                        // Record format:
                        //
                        //     int32 -- class serial number

                        int classSerialNumber = buffer.getInt();
                        String className = classNames.get(classSerialNumber);
                        return new UnloadClassRecord(
                                that, typeTag, baseTimestamp, timestampOffset,
                                classSerialNumber, className
                        );

                    } else if (RECORD_ALLOC_SITES == typeTag) {
                        // TODO: Implement this.
                    } else if (RECORD_HEAP_SUMMARY == typeTag) {
                        // This is a terminating record and should return a HeapSummaryRecord.
                        // Record format:
                        //
                        //     int32 -- total number of live bytes
                        //     int32 -- total number of live instances
                        //     int64 -- total number of bytes allocated
                        //     int64 -- total number of instances allocated

                        int nLiveBytes = buffer.getInt();
                        int nLiveInstances = buffer.getInt();
                        long nTotalBytes = buffer.getLong();
                        long nTotalInstances = buffer.getLong();

                        return new HeapSummaryRecord(
                                that, typeTag, baseTimestamp, timestampOffset,
                                nLiveBytes, nLiveInstances,
                                nTotalBytes, nTotalInstances
                        );

                    } else if (RECORD_HEAP_DUMP == typeTag) { // TODO: What should this be?
                    } else if (RECORD_HEAP_DUMP_SEGMENT == typeTag) {
                        // TODO: Implement reading segments until RECORD_HEAP_DUMP_END
                    } else if (RECORD_CPU_SAMPLES == typeTag) {
                        // This is a terminating record and should return a CpuSamplesRecord.
                        // Record format:
                        //
                        //     int32 -- total number of samples
                        //     int32 -- number of samples sets that follow
                        //     struct* -- samples
                        //       int32 -- number of samples in this set
                        //       int32 -- serial number of a stack trace
                        // TODO: I'm not sure I believe this.

                        // TODO: Implement.
                    } else if (RECORD_CONTROL_SETTINGS == typeTag) {
                        // Record format:
                        //
                        //    int32 -- bitmask: 0x1=alloc traces on/off; 0x2=cpu sampling on/off
                        //    int16 -- stack trace depth

                        int bitmask = buffer.getInt();
                        short stackTraceDepth = buffer.getShort();

                        return new ControlSettingsRecord(
                                that, typeTag, baseTimestamp, timestampOffset,
                                (bitmask & 0x1) != 0, (bitmask & 0x2) != 0, stackTraceDepth
                        );
                    } else {
                        // Unknown record. Return it and hope the use knows what to do with it.
                        // Record format:
                        //
                        //   sequence of bytes

                        byte[] body = new byte[nBytes];
                        buffer.get(body);

                        return new UnknownRecord(that, typeTag, baseTimestamp, timestampOffset, body);
                    }
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove from an HProfRecord iterator.");

            }
        };
    }


}
