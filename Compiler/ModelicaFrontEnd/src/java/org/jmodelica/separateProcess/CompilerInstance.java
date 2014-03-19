package org.jmodelica.separateProcess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jmodelica.util.logging.IllegalLogStringException;

public class CompilerInstance {

    private String javaHome;
    private Collection<String> classPath = new ArrayList<String>();
    private String jvmArguments;
    private Compiler compiler = Compiler.MODELICA;
    private String target;
    private String version;
    private Collection<String> modelicaPath = new ArrayList<String>();
    private Collection<String> log = new ArrayList<String>();
    private Map<String, String> compilerOptions = new HashMap<String, String>();
    private String platform;
    private String outputPath;
    private String jmodelicaHome;

    /**
     * Constructor for CompilerInterface that takes JModelica home from the environment.
     */
    public CompilerInstance() {
        this(System.getenv("JMODELICA_HOME"));
    }

    /**
     * Constructor for CompilerInterface that takes JModelica home as argument.
     * 
     * @param jmodelicaHome Path to a valid JModelica installation
     */
    public CompilerInstance(String jmodelicaHome) {
        this.jmodelicaHome = jmodelicaHome;
        if (!new File(joinPath(jmodelicaHome, "lib", "ModelicaCompiler.jar")).exists())
            throw new IllegalArgumentException("JMODELICA_HOME does not point to a valid jmodelica installation!");
        setJavaHome(System.getProperty("java.home"));
        setClassPath(joinPath(jmodelicaHome, "lib", "ModelicaCompiler.jar"), joinPath(jmodelicaHome, "lib", "OptimicaCompiler.jar"), joinPath(jmodelicaHome, "lib", "util.jar"), joinPath(jmodelicaHome, "ThirdParty", "Beaver", "lib", "beaver-rt.jar"));
        setTarget("cs");
        setModelicaPath(joinPath(jmodelicaHome, "ThirdParty", "MSL"));
        setLog("w");
    }

    /**
     * Set java installation that is used to compile in separate process.
     * 
     * @param javaHome Path to valid java installation
     */
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    /**
     * Path to java installation that is used to compile in separate process.
     * 
     * @return Path to java installation
     */
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * TODO
     * @param paths
     */
    public void setClassPath(String... paths) {
        classPath.clear();
        addClassPath(paths);
    }

    public void addClassPath(String... paths) {
        if (paths != null) {
            for (String path : paths)
                classPath.add(path);
        }
    }

    public Iterator<String> getClassPath() {
        return classPath.iterator();
    }

    public void setJvmArguments(String jvmArguments) {
        this.jvmArguments = jvmArguments;
    }

    public String getJvmArguments() {
        return jvmArguments;
    }

    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    public Compiler getCompiler() {
        return compiler;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setModelicaPath(String... paths) {
        modelicaPath.clear();
        addModelicaPath(paths);
    }

    public void addModelicaPath(String... paths) {
        if (paths != null) {
            for (String path : paths)
                modelicaPath.add(path);
        }
    }

    public Iterator<String> getModelicaPath() {
        return modelicaPath.iterator();
    }

    public void setLog(String... logs) {
        log.clear();
        addLog(logs);
    }

    public void addLog(String... logs) {
        if (logs != null) {
            for (String log : logs) {
                if (log.contains("|stderr"))
                    throw new IllegalLogStringException("Piping compiler log to stderr is not allowed while separate process is used.", null);
                this.log.add(log);
            }
        }
    }

    public Iterator<String> getLog() {
        return log.iterator();
    }

    public void setOptions(Map<String, String> options) {
        compilerOptions.clear();
        addOptions(options);
    }

    public void addOptions(Map<String, String> options) {
        compilerOptions.putAll(options);
    }

    public void addOption(String option, String value) {
        compilerOptions.put(option, value);
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    private List<String> buildArgs(String modelName, String sourceFiles[]) {
        List<String> args = new ArrayList<String>();
        args.add(joinPath(javaHome, "bin", "java"));
        if (classPath.size() > 0) {
            args.add("-cp");
            args.add(join(File.pathSeparator, classPath));
        }
        if (jvmArguments != null)
            args.add(jvmArguments);
        args.add(compiler.className);
        if (target != null)
            args.add("-target=" + target);
        if (version != null)
            args.add("-version=" + version);
        if (modelicaPath.size() > 0)
            args.add("-modelicapath=" + join(File.pathSeparator, modelicaPath));
        if (log.size() > 0)
            args.add("-log=w|os|stderr," + join(",", log));
        else
            args.add("-log=w,w|os|stderr");
        if (compilerOptions.size() > 0)
            args.add("-options=" + join(",", ":", compilerOptions));
        if (platform != null)
            args.add("-platform=" + platform);
        if (outputPath != null)
            args.add("-out=\"" + outputPath + "\"");
        args.add("," + join(",", sourceFiles));
        args.add(modelName);
        return args;
    }

    public Compilation compile(String modelName, String... sourceFiles) throws IOException {
        return new Compilation(buildArgs(modelName, sourceFiles), jmodelicaHome);
    }

    public static void main(String[] args) throws Throwable {
        CompilerInstance ci = new CompilerInstance();
        Compilation c = ci.compile("Modelica.Mechanics.MultiBody.Examples.Elementary.FreeBody");
        c.join();
    }

    private static String join(String delimiter, String... args) {
        if (args == null)
            return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String str : args) {
            if (!first)
                sb.append(delimiter);
            first = false;
            sb.append(str);
        }
        return sb.toString();
    }

    private static String joinPath(String... args) {
        return join(File.separator, args);
    }

    private static String join(String delimiter, Collection<String> args) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String str : args) {
            if (!first)
                sb.append(delimiter);
            first = false;
            sb.append(str);
        }
        return sb.toString();
    }

    private static String join(String delimiter, String pairDelimiter, Map<String, String> args) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> pair : args.entrySet()) {
            if (!first)
                sb.append(delimiter);
            first = false;
            sb.append(pair.getKey());
            sb.append(pairDelimiter);
            sb.append(pair.getValue());
        }
        return sb.toString();
    }

    public enum Compiler {
        MODELICA("org.jmodelica.modelica.compiler.ModelicaCompiler"), OPTIMICA("org.jmodelica.optimica.compiler.OptimicaCompiler");

        private final String className;

        private Compiler(String classPath) {
            this.className = classPath;
        }
    }

}
