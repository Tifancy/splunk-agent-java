package com.splunk.javaagent.hprof2;

public class StackFrame {
    private String methodName;
    private String methodSignature;
    private String sourceFile;
    private int classSerialNumber;
    private String className;
    private Integer lineNumber;

    public StackFrame(String methodName, String methodSignature, String sourceFile,
                      int classSerialNumber, String className, Integer lineNumber) {
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
    public int getClassSerialNumber() { return classSerialNumber; }
    public String getClassName() { return className; }
    public Integer getLineNumber() { return lineNumber; }
}
