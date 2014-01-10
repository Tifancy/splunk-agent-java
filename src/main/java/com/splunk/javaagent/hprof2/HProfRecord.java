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

import com.splunk.javaagent.hprof.*;
import com.sun.org.apache.bcel.internal.generic.LoadClass;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class HProfRecord {
    public static final byte RECORD_UTF8 = 1;
    public static final byte RECORD_LOAD_CLASS = 2;
    public static final byte RECORD_UNLOAD_CLASS = 3;
    public static final byte RECORD_FRAME = 4;
    public static final byte RECORD_TRACE = 5;
    public static final byte RECORD_ALLOC_SITES = 6;
    public static final byte RECORD_HEAP_SUMMARY = 7;
    public static final byte RECORD_START_THREAD = 10;
    public static final byte RECORD_END_THREAD = 11;
    public static final byte RECORD_CPU_SAMPLES = 13;
    public static final byte RECORD_CONTROL_SETTINGS = 14;
    public static final byte RECORD_HEAP_DUMP = 12;
    public static final byte RECORD_HEAPDUMP_SEGMENT_START = 28;
    public static final byte RECORD_HEAPDUMP_SEGMENT_END = 44;

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

    private HProfDumpFile parent;
    private byte typeTag;
    private long baseTimestamp;
    private long timestampOffset;

    public HProfRecord(HProfDumpFile parent, byte typeTag, long baseTimestamp, long timestampOffset) {
        this.parent = parent;
        this.typeTag = typeTag;
        this.baseTimestamp = baseTimestamp;
        this.timestampOffset = timestampOffset;
    }

    public byte getTypeTag() {
        return this.typeTag;
    }

    public long getBaseTimestamp() {
        return this.baseTimestamp;
    }

    public long getTimestampOffset() {
        return this.timestampOffset;
    }
}
