package com.splunk.javaagent.hprof2;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class HProfDumpFileTest {
    File hprofFile;

    List<File> temporaryFiles = new ArrayList<File>();

    public File resourceToTemporaryFile(String path) throws IOException {
        File output = File.createTempFile("splunk-agent-java-", ".hprof");

        InputStream stream = ClassLoader.getSystemResourceAsStream(path);

        BufferedInputStream bin = new BufferedInputStream(stream);
        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(output));

        int ch;
        while ((ch = bin.read()) != -1) {
            bout.write(ch);
        }
        bout.flush();

        bin.close();
        bout.close();

        temporaryFiles.add(output);

        return output;
    }

    @After
    public void tearDown() {
        for (File f : temporaryFiles) {
            f.delete();
        }
    }

    public void putUnsignedInt32(ArrayList<Byte> buffer, long value) {
        buffer.add((byte) ((value & (0xFFL << 24)) >> 24));
        buffer.add((byte) ((value & (0xFFL << 16)) >> 16));
        buffer.add((byte) ((value & (0xFFL << 8)) >> 8));
        buffer.add((byte) (value & 0xFFL));
    }

    public void putLong(ArrayList<Byte> buffer, long value) {
        buffer.add((byte) ((value & (0xFFL << 56)) >> 56));
        buffer.add((byte) ((value & (0xFFL << 48)) >> 48));
        buffer.add((byte) ((value & (0xFFL << 40)) >> 40));
        buffer.add((byte) ((value & (0xFFL << 32)) >> 32));
        buffer.add((byte) ((value & (0xFFL << 24)) >> 24));
        buffer.add((byte) ((value & (0xFFL << 16)) >> 16));
        buffer.add((byte) ((value & (0xFFL << 8)) >> 8));
        buffer.add((byte) (value & 0xFFL));
    }

    public void putID(ArrayList<Byte> buffer, long identifierSize, long value) {
        if (identifierSize == 4) {
            putUnsignedInt32(buffer, value);
        } else {
            putLong(buffer, value);
        }
    }

    @Test
    public void readWriteLongWorks() {
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        long expected = 24294967290L;
        putLong(bytes, expected);

        ByteBuffer buffer = arrayListToByteBuffer(bytes);
        Assert.assertEquals(expected, buffer.getLong());
    }

    public ByteBuffer arrayListToByteBuffer(ArrayList<Byte> in) {
        Byte[] t = in.toArray(new Byte[] {});
        byte[] u = new byte[t.length];
        for (int i = 0; i < t.length; i++) {
            u[i] = t[i];
        }
        return ByteBuffer.wrap(u);
    }

    @Test
    public void unsignedInt32ReadWrite() {
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        putUnsignedInt32(bytes, 1025L);
        ByteBuffer buffer = arrayListToByteBuffer(bytes);

        long found = HProfDumpFile.getUnsignedInt32(buffer);
        Assert.assertEquals(found, 1025L);
    }

    public static byte[] toUtf8Bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Your JVM should have UTF-8.");
        }
    }

    public static final byte[] expectedFormatString = toUtf8Bytes("JAVA PROFILE 1.0.2\0");

    public void writeHeader(ArrayList<Byte> buffer, long identifierSize, long baseTimestamp) {
        for (byte b : expectedFormatString)
            buffer.add(b);
        putUnsignedInt32(buffer, identifierSize);
        putLong(buffer, baseTimestamp);
    } 
    public long writeUtf8Record(ArrayList<Byte> buffer, long identifierSize, long timeOffset, long stringId, String string) {
        if (identifierSize != 4 && identifierSize != 8) {
            throw new RuntimeException("Invalid identifier size. Must be 4 or 8.");
        }

        buffer.add((byte)0x01);

        putUnsignedInt32(buffer, timeOffset);

        byte[] stringBytes = toUtf8Bytes(string);

        long length = identifierSize + stringBytes.length;
        putUnsignedInt32(buffer, length);

        if (identifierSize == 4) {
            putUnsignedInt32(buffer, stringId);
        } else {
            putLong(buffer, stringId);
        }

        for (byte b : stringBytes) {
            buffer.add(b);
        }
        return stringId;
    }

    public long writeStackFrame(ArrayList<Byte> buffer, long identifierSize, long timeOffset,
                                long stackframeId, long methodNameStringId, long methodSignatureStringId,
                                long sourceFileNameStringId, long classSerialNumber, long lineNumber) {
        // Header
        buffer.add((byte)0x04);
        putUnsignedInt32(buffer, timeOffset);
        putUnsignedInt32(buffer, 4*identifierSize + 8);

        // Body
        putID(buffer, identifierSize, stackframeId);
        putID(buffer, identifierSize, methodNameStringId);
        putID(buffer, identifierSize, methodSignatureStringId);
        putID(buffer, identifierSize, sourceFileNameStringId);
        putUnsignedInt32(buffer, classSerialNumber);
        putUnsignedInt32(buffer, lineNumber);
        return stackframeId;
    }

    public long writeStackTrace(ArrayList<Byte> buffer, long identifierSize, long timeOffset,
                                long stackTraceSerialNumber, long threadSerialNumber,
                                List<Long> stackFrameIds) {
        // Header
        buffer.add((byte)0x05);
        putUnsignedInt32(buffer, timeOffset);
        putUnsignedInt32(buffer, 12 + identifierSize*stackFrameIds.size());

        // Body
        putUnsignedInt32(buffer, stackTraceSerialNumber);
        putUnsignedInt32(buffer, threadSerialNumber);
        putUnsignedInt32(buffer, (long)stackFrameIds.size());
        for (long sid : stackFrameIds) {
            putID(buffer, identifierSize, sid);
        }

        return stackTraceSerialNumber;
    }

    public void writeLoadClass(ArrayList<Byte> buffer, long identifierSize, long timeOffset,
                               long classSerialNumber, long classObjectId, long stackTraceSerialNumber,
                               long classNameStringId) {
        // Header
        buffer.add((byte)0x02);
        putUnsignedInt32(buffer, timeOffset);
        putUnsignedInt32(buffer, 8 + 2*identifierSize);

        // Body
        putUnsignedInt32(buffer, classSerialNumber);
        putID(buffer, identifierSize, classObjectId);
        putUnsignedInt32(buffer, stackTraceSerialNumber);
        putID(buffer, identifierSize, classNameStringId);
    }


    public static final int HEADER_SIZE = 19 + 12;

    @Test
    public void readsFormatVersion() throws IOException, ParseException {
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        writeHeader(bytes, 4, 0);
        ByteBuffer buffer = arrayListToByteBuffer(bytes);

        HProfDumpFile h = new HProfDumpFile(buffer);

        Assert.assertEquals("JAVA PROFILE 1.0.2", h.getFormatString());
    }

    @Test(expected=java.text.ParseException.class)
    public void readInvalidFormatVersion() throws IOException, ParseException {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.put("abcdefghijabcdefghij".getBytes("UTF-8"));
        buffer.flip();

        HProfDumpFile h = new HProfDumpFile(buffer);
    }

    @Test
    public void readsIdentifierOfSize4() throws Exception {
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        writeHeader(bytes, 4, 0);
        ByteBuffer buffer = arrayListToByteBuffer(bytes);

        HProfDumpFile h = new HProfDumpFile(buffer);
        Assert.assertEquals(4L, h.getIdentifierSize());
    }

    @Test
    public void readsIdentifierOfSize8() throws Exception {
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        writeHeader(bytes, 8L, 0);
        ByteBuffer buffer = arrayListToByteBuffer(bytes);

        HProfDumpFile h = new HProfDumpFile(buffer);
        Assert.assertEquals(8L, h.getIdentifierSize());
    }

    @Test(expected=ParseException.class)
    public void failsOnInvalidIdentifierSize() throws ParseException, IOException {
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        writeHeader(bytes, 6L, 0);
        ByteBuffer buffer = arrayListToByteBuffer(bytes);

        HProfDumpFile h = new HProfDumpFile(buffer);
    }

    @Test
    public void readsBaseTimestamp() throws IOException, ParseException {
        long[] numbers = new long[] { 2L, 552L, 36221L, 2147483640L, 4294967290L };

        for (long expected : numbers) {
            ArrayList<Byte> bytes = new ArrayList<Byte>();
            writeHeader(bytes, 4, expected);
            ByteBuffer buffer = arrayListToByteBuffer(bytes);

            HProfDumpFile h = new HProfDumpFile(buffer);
            Assert.assertEquals(expected, h.getBaseTimestamp());
        }
    }

    @Test
    public void getUnsignedInt32WorksWithSignedInt32Range() {
        int[] numbers = new int[] { 2, 552, 36221, 2147483640 };

        for (int expected : numbers) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(expected);

            buffer.flip();

            long found = HProfDumpFile.getUnsignedInt32(buffer);
            Assert.assertEquals((long)expected, found);
        }
    }

    @Test
    public void getUnsignedInt32WorksWithUnsignedRange() {
        long expected = 4294967290L;

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(expected);

        buffer.flip();

        buffer.getInt(); // Skip 4 bytes
        long found = HProfDumpFile.getUnsignedInt32(buffer);
        Assert.assertEquals((long)expected, found);
    }

    @Test
    public void stringReadCorrectly() throws IOException, ParseException {
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        long identifierSize = 4;
        writeHeader(bytes, identifierSize, 0);

        String methodString = "myMethod";
        long methodId = writeUtf8Record(bytes, identifierSize, 0, 125, methodString);

        String signatureString = "(int, String)";
        long signatureId = writeUtf8Record(bytes, identifierSize, 0, 255, signatureString);

        String sourceFileString = "MyClass.java";
        long sourceFileId = writeUtf8Record(bytes, identifierSize, 0, 257, sourceFileString);

        String classNameString = "MyClass";
        long classNameId = writeUtf8Record(bytes, identifierSize, 0, 11, classNameString);

        long stackFrameId = writeStackFrame(bytes, identifierSize, 12, 55, methodId, signatureId,
                sourceFileId, 55, 251);
        ArrayList<Long> stackFrames = new ArrayList<Long>();
        stackFrames.add(stackFrameId);
        long stackTraceId = writeStackTrace(bytes, identifierSize, 12, 8, 55, stackFrames);

        writeLoadClass(bytes, identifierSize, 0, 55, 112, stackTraceId, classNameId);
        ByteBuffer buffer = arrayListToByteBuffer(bytes);

        HProfDumpFile h = new HProfDumpFile(buffer);
        for (HProfRecord r : h) {}

        Assert.assertEquals(4, h.getUtf8Strings().size());

        Assert.assertTrue(h.getUtf8Strings().containsKey(methodId));
        Assert.assertEquals(methodString, h.getUtf8Strings().get(methodId));

        Assert.assertTrue(h.getUtf8Strings().containsKey(signatureId));
        Assert.assertEquals(signatureString, h.getUtf8Strings().get(signatureId));

        Assert.assertTrue(h.getUtf8Strings().containsKey(sourceFileId));
        Assert.assertEquals(sourceFileString, h.getUtf8Strings().get(sourceFileId));

        Assert.assertTrue(h.getUtf8Strings().containsKey(classNameId));
        Assert.assertEquals(classNameString, h.getUtf8Strings().get(classNameId));
    }

    // TODO: Tests to write:
    // - Are strings properly recorded?
    // - Are stack frames properly recorded? Do they have their className?
    // - Are stack frames properly recorded if they can't find classNames?
    // - Are stack traces properly recorded?
    // - Is a ClassLoadRecord created properly? Does it leave its className registered?
    // - Is a ClassUnloadRecord created properly? Does it properly find its className?
    // - Does a ClassUnloadRecord get created properly if it can't find its className?
    // - Test plan around AllocSitesRecords needs to be written.
    // - Are HeapSummaryRecord objects properly created?
    // - Test plan around heap dumps and segmented heap dumps.
    // - Are ControlSettingsRecords properly created?
    // - Are CpuSamplesRecords properly created? Do they get their stack traces inlined sensibly?
}
