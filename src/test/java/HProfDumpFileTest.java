import com.splunk.javaagent.hprof2.HProfDumpFile;
import com.splunk.javaagent.hprof2.HProfRecord;
import com.splunk.javaagent.hprof2.LoadClassRecord;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
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

    @Test
    public void readsFormatVersion() throws IOException, ParseException {
        HProfDumpFile h = new HProfDumpFile(resourceToTemporaryFile("example_hprof"));
        Assert.assertEquals("JAVA PROFILE 1.0.1", h.getFormatString());
    }

    @Test(expected=java.text.ParseException.class)
    public void readInvalidFormatVersion() throws IOException, ParseException {
        HProfDumpFile h = new HProfDumpFile(resourceToTemporaryFile("invalid_hprof"));
    }

    @Test
    public void readsIdentifierSize() throws Exception {
        HProfDumpFile h = new HProfDumpFile(resourceToTemporaryFile("example_hprof"));
        String bitwidth = System.getProperty("sun.arch.data.model");
        int expected;
        if (bitwidth == null)
            throw new Exception("You're not on the Oracle JVM and we can't find your architecture easily.");
        if (bitwidth.equals("32"))
            expected = 4;
        else if (bitwidth.equals("64"))
            expected = 8;
        else
            throw new Exception("What kind of architecture are you on?!?");
        Assert.assertEquals(expected, h.getIdentifierSize());
    }

    @Test
    public void readsBaseTimestamp() throws IOException, ParseException {
        HProfDumpFile h = new HProfDumpFile(resourceToTemporaryFile("example_hprof"));
        long expectedBaseTimestamp = 1389302764578L;

        Assert.assertEquals(expectedBaseTimestamp, h.getBaseTimestamp());
    }

    @Test
    public void iteratorWorks() throws IOException, ParseException {
        HProfDumpFile h = new HProfDumpFile(resourceToTemporaryFile("example_hprof"));

        for (HProfRecord record : h) {

        }
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
