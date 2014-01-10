package com.splunk.javaagent.hprof2;

public class StackFrame {
    private String methodName;
    private String methodSignature;
    private String sourceFile;
    private long classSerialNumber;
    private String className;
    private Long lineNumber;

    public StackFrame(String methodName, String methodSignature, String sourceFile,
                      long classSerialNumber, String className, Long lineNumber) {
        this.methodName = methodName;
        this.methodSignature = methodSignature;
        this.sourceFile = sourceFile;
        this.classSerialNumber = classSerialNumber;
        this.className = className;
        this.lineNumber = lineNumber;
    }

    public String getMethodName() { return methodName; }
    public String getMethodSignature() { return methodSignature; }
    public String getSourceFile() { return sourceFile; }
    public long getClassSerialNumber() { return classSerialNumber; }
    public String getClassName() { return className; }
    public Long getLineNumber() { return lineNumber; }
}
