package com.splunk.javaagent;

import com.splunk.javaagent.trace.SplunkClassFileTransformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class SplunkJavaAgent {

    private static final String DEFAULT_PROPERTIES_FILE = "default.properties";

	public static void premain(String arg, Instrumentation instrumentation) {
        AgentConfiguration configuration = new AgentConfiguration();

        File propertiesFile = new File(arg);
        if (!propertiesFile.exists()) {
            try {
                configuration.load(ClassLoader.getSystemResourceAsStream(DEFAULT_PROPERTIES_FILE));
            } catch (IOException e) {
                System.err.println("Agent could not find its default config file. Failing.");
                System.exit(100);
            }
        } else {
            try {
                configuration.load(new FileInputStream(propertiesFile));
            } catch (IOException e) {
                System.err.println("Agent could not open config file " + arg + "; failing.");
                System.exit(100);
            }
        }

        // Open Splunk TCP port writer

        // initJMX

        // TODO: Create a timertask which dumps HProf files to a temporary file, then loads them and writes them to Splunk.
        // Schedule it. Add call to cancel it to shutdown hook below.

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                // Stop Splunk logging and close that port
            }
        });

        instrumentation.addTransformer(new SplunkClassFileTransformer());

        // finally:
        //   interrupt HProf thread
        //   interrupt JMX thread
	}


     /*

	private boolean initHprof() {
        // Fields:
        //   trace.hprof :: Boolean
        //   trace.hprof.tempfile :: String
        //   trace.hprof.frequency :: Integer
        //   trace.hprof.recordtypes :: RecordType[:SubTypes],*

        // Start HprofThread, passed current thread, frequency, and dumpfile.

		this.traceHprof = Boolean.parseBoolean(agent.props.getProperty(
				"trace.hprof", "false"));
		if (this.traceHprof) {
			this.hprofFile = props.getProperty("trace.hprof.tempfile", "");
			try {
				this.hprofFrequency = Integer.parseInt(props.getProperty(
						"trace.hprof.frequency", "600"));
			} catch (NumberFormatException e) {

			}
			String hprofRecordFilterString = props.getProperty(
					"trace.hprof.recordtypes", "");
			if (hprofRecordFilterString.length() >= 1) {
				this.hprofRecordFilter = new ArrayList<Byte>();
				this.hprofHeapDumpSubRecordFilter = new HashMap<Byte, List<Byte>>();
				StringTokenizer st = new StringTokenizer(
						hprofRecordFilterString, ",");
				while (st.hasMoreTokens()) {
					StringTokenizer st2 = new StringTokenizer(st.nextToken(),
							":");

					byte val = Byte.parseByte(st2.nextToken());
					this.hprofRecordFilter.add(val);
					// subrecords
					if (st2.hasMoreTokens()) {
						byte subVal = Byte.parseByte(st2.nextToken());
						List<Byte> list = this.hprofHeapDumpSubRecordFilter
								.get(val);
						if (list == null) {
							list = new ArrayList<Byte>();
						}
						list.add(subVal);
						this.hprofHeapDumpSubRecordFilter.put(val, list);
					}

				}
			}
			try {
				HprofThread thread = new HprofThread(Thread.currentThread(),
						this.hprofFrequency, this.hprofFile);
				thread.start();
			} catch (Exception e) {
			}
		}

		return true;
	}

	private boolean initJMX() {

		this.traceJMX = Boolean.parseBoolean(agent.props.getProperty(
				"trace.jmx", "false"));
		if (this.traceJMX) {

			this.jmxConfigFiles = new HashMap<String, Integer>();
			String configFiles = props.getProperty("trace.jmx.configfiles", "");
			String defaultFrequency = props.getProperty(
					"trace.jmx.default.frequency", "60");
			StringTokenizer st = new StringTokenizer(configFiles, ",");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				String frequency = props.getProperty("trace.jmx." + token
						+ ".frequency", defaultFrequency);
				this.jmxConfigFiles.put(token + ".xml",
						Integer.parseInt(frequency));
			}

			Set<String> configFileNames = this.jmxConfigFiles.keySet();
			for (String configFile : configFileNames) {
				JMXThread thread = new JMXThread(Thread.currentThread(),
						this.jmxConfigFiles.get(configFile), configFile);
				thread.start();
			}
		}

		return true;
	}


	class TransporterThread extends Thread {

		Thread parent;

		TransporterThread(Thread parent) {
			this.parent = parent;
		}

		public void run() {

			while (parent.isAlive()) {

				try {
					while (!agent.eventQueue.isEmpty()) {
						SplunkLogEvent event = agent.eventQueue.poll();

						if (event != null) {
							agent.transport.send(event);
						}
					}
				} catch (Throwable t) {

				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {

				}
			}
		}

	}

	class JMXThread extends Thread {

		Thread parent;
		int frequencySeconds;
		String configFile;
		JMXMBeanPoller poller;

		JMXThread(Thread parent, int frequencySeconds, String configFile) {
			this.parent = parent;
			this.configFile = configFile;
			this.frequencySeconds = frequencySeconds;
			this.poller = new JMXMBeanPoller(configFile);

		}

		public void run() {

			while (parent.isAlive()) {

				try {

					Thread.sleep(frequencySeconds * 1000);
				} catch (InterruptedException e) {
				}

				try {
					poller.execute();

				} catch (Throwable t) {

				}

			}
		}

	}

	class HprofThread extends Thread {

		Thread parent;
		int frequencySeconds;
		String hprofFile;
		MBeanServerConnection serverConnection;
		ObjectName mbean;
		String operationName;
		Object[] params;
		String[] signature;

		HprofThread(Thread parent, int frequencySeconds, String hprofFile)
				throws Exception {
			this.parent = parent;
			this.hprofFile = hprofFile;
			this.frequencySeconds = frequencySeconds;
			this.serverConnection = ManagementFactory.getPlatformMBeanServer();
			this.mbean = new ObjectName(
					"com.sun.management:type=HotSpotDiagnostic");
			this.operationName = "dumpHeap";
			this.params = new Object[2];
			this.params[0] = hprofFile;
			this.params[1] = new Boolean(true);
			this.signature = new String[2];
			this.signature[0] = "java.lang.String";
			this.signature[1] = "boolean";
		}

		public void run() {

			while (parent.isAlive()) {
				try {

					Thread.sleep(frequencySeconds * 1000);
				} catch (InterruptedException e) {
				}
				try {
					// do some housekeeping
					File file = new File(this.hprofFile);
					if (file.exists())
						file.delete();

					// do the dump via JMX
					serverConnection.invoke(mbean, operationName, params,
							signature);

					// process the dump
					file = new File(this.hprofFile);
					HprofDump hprof = new HprofDump(file);
					hprof.process();

					// delete the dump files
					if (file.exists())
						file.delete();

				} catch (Throwable e) {

				}

			}
		}

	}
    /*
   	private boolean initTransport() {

		try {
			this.transport = (SplunkTransport) Class
					.forName(
							props.getProperty("splunk.transport.impl",
									"com.splunk.javaagent.transport.SplunkTCPTransport"))
					.newInstance();
		} catch (Exception e) {

			return false;
		}
		Map<String, String> args = new HashMap<String, String>();
		Set<Object> keys = props.keySet();
		for (Object key : keys) {
			String keyString = (String) key;
			if (keyString.startsWith("splunk."))
				args.put(keyString, props.getProperty(keyString));
		}

		try {
			this.queueSize = Integer.parseInt(props.getProperty(
					"splunk.transport.internalQueueSize", "10000"));
		} catch (NumberFormatException e) {

		}

		try {

			this.eventQueue = new ArrayBlockingQueue<SplunkLogEvent>(queueSize);
			this.transport.init(args);
			this.transport.start();
			this.transporterThread = new TransporterThread(
					Thread.currentThread());
			this.transporterThread.start();

		} catch (Exception e) {

			return false;
		}
		return true;

	}


	public static void classLoaded(String className) {

		if (agent.traceClassLoaded) {
			SplunkLogEvent event = new SplunkLogEvent("class_loaded",
					"splunkagent", true, false);
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			event.addPair("className", className);
			addUserTags(event);
			agent.transport.send(event);
		}
	}

	private static void addUserTags(SplunkLogEvent event) {

		if (!agent.userTags.isEmpty()) {

			Set<String> keys = agent.userTags.keySet();
			for (String key : keys) {
				event.addPair(key, agent.userTags.get(key));
			}
		}

	}

	public static void methodEntered(String className, String methodName,
			String desc) {

		if (agent.traceMethodEntered) {
			SplunkLogEvent event = new SplunkLogEvent("method_entered",
					"splunkagent", true, false);
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			event.addPair("className", className);
			event.addPair("methodName", methodName);
			event.addPair("methodDesc", desc);
			event.addPair("threadID", Thread.currentThread().getId());
			event.addPair("threadName", Thread.currentThread().getName());

			try {
				StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
				if (ste != null)
					event.addPair("lineNumber", ste.getLineNumber());
				event.addPair("sourceFileName", ste.getFileName());
			} catch (Exception e1) {
			}

			addUserTags(event);
			try {
				agent.eventQueue.put(event);
				// agent.eventQueue.offer(event,1000,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {

			}
		}
	}

	public static void methodExited(String className, String methodName,
			String desc) {

		if (agent.traceMethodExited) {

			SplunkLogEvent event = new SplunkLogEvent("method_exited",
					"splunkagent", true, false);
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			event.addPair("className", className);
			event.addPair("methodName", methodName);
			event.addPair("methodDesc", desc);
			event.addPair("threadID", Thread.currentThread().getId());
			event.addPair("threadName", Thread.currentThread().getName());
			addUserTags(event);
			try {
				agent.eventQueue.put(event);
				// agent.eventQueue.offer(event,1000,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {

			}
		}
	}

	public static void throwableCaught(String className, String methodName,
			String desc, Throwable t) {

		if (agent.traceErrors) {

			SplunkLogEvent event = new SplunkLogEvent("throwable_caught",
					"splunkagent", true, false);
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			event.addPair("className", className);
			event.addPair("methodName", methodName);
			event.addPair("methodDesc", desc);
			event.addThrowable(t);
			event.addPair("methodName", methodName);
			event.addPair("threadID", Thread.currentThread().getId());
			event.addPair("threadName", Thread.currentThread().getName());
			addUserTags(event);
			try {
				agent.eventQueue.put(event);
				// agent.eventQueue.offer(event,1000,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {

			}

		}
	}

	public static void hprofRecordEvent(byte recordType, byte subRecordType,
			SplunkLogEvent event) {

		if (traceHprofRecordType(recordType, subRecordType)) {
			event.addPair("appName", agent.appName);
			event.addPair("appID", agent.appID);
			addUserTags(event);
			try {

				agent.eventQueue.put(event);

			} catch (InterruptedException e) {

			}

		}
	}

	public static void jmxEvent(SplunkLogEvent event) {

		event.addPair("appName", agent.appName);
		event.addPair("appID", agent.appID);
		addUserTags(event);
		try {

			agent.eventQueue.put(event);

		} catch (InterruptedException e) {

		}

	}

	private static boolean traceHprofRecordType(byte recordType,
			byte subRecordType) {
		if (agent.hprofRecordFilter == null
				|| agent.hprofRecordFilter.isEmpty())
			return true;
		else {
			for (byte b : agent.hprofRecordFilter) {
				if (b == recordType) {
					List<Byte> subrecords = agent.hprofHeapDumpSubRecordFilter
							.get(recordType);
					if (subrecords == null || subrecords.isEmpty()) {
						return true;
					} else {
						for (byte bb : subrecords) {
							if (bb == subRecordType) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}*/

}
