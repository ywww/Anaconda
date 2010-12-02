/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.scilab.worker;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.utils.OperatingSystem;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfig;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciJVMInfo;
import org.ow2.proactive.scheduler.ext.matsci.worker.MatSciTask;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciTaskServerConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig;
import org.ow2.proactive.scheduler.ext.scilab.worker.util.ScilabEngineConfig;
import org.ow2.proactive.scheduler.ext.scilab.worker.util.ScilabFinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;


/**
 * This class represents a Scilab-specific Task inside the Scheduler
 * @author The ProActive Team
 */
public class ScilabTask<W extends AOScilabWorker> extends
        MatSciTask<W, ScilabEngineConfig, PASolveScilabGlobalConfig, PASolveScilabTaskConfig> {

    final private static String[] DEFAULT_OUT_VARIABLE_SET = { "out" };

    /**
     * This hostname, for debugging purpose
     */
    protected String host;

    /**
     * the lines of inputScript
     */
    protected String inputScript = null;

    /**
     * Definition of user functions
     */
    protected String functionsDefinition = null;

    /**
     * Scilab Function name
     */

    protected String functionName = null;

    /**
     * The lines of the Scilab script
     */
    protected ArrayList<String> scriptLines = null;

    /**
     * Node name where this task is being executed
     */
    protected String nodeName = null;

    /**
     * the array of output variable names
     */
    protected String[] out_set = DEFAULT_OUT_VARIABLE_SET;

    /**
     * holds the Scilab environment information
     */
    protected static ScilabEngineConfig scilabConfig = null;

    /**
     * the OS where this JVM is running
     */
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    /**
     * ProActive No Arg Constructor
     */
    public ScilabTask() {
    }

    @Override
    protected MatSciEngineConfig initEngineConfig(MatSciJVMInfo info) throws Throwable {
        if (scilabConfig == null) {
            // First we try to find SCILAB
            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] launching script to find Scilab");
                outDebug.println("[" + new java.util.Date() + " " + host +
                    " ScilabTask] launching script to find Scilab");
            }
            scilabConfig = (ScilabEngineConfig) ScilabFinder.getInstance().findMatSci(
                    paconfig.getVersionPref(), paconfig.getVersionRej(), paconfig.getVersionMin(),
                    paconfig.getVersionMax());

            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " ScilabTask] " + scilabConfig);
                outDebug.println("[" + new java.util.Date() + " " + host + " ScilabTask] " + scilabConfig);
            }
            if (scilabConfig == null)
                throw new IllegalStateException("No valid Scilab configuration found, aborting...");

        } else {
            ScilabEngineConfig conf = ScilabEngineConfig.getCurrentConfiguration();
            if (conf != null) {
                scilabConfig = conf;
            }
        }
        info.setConfig(scilabConfig);
        return scilabConfig;
    }

    protected MatSciTaskServerConfig getTaskServerConfig() {
        return new MatSciTaskServerConfig(true, -1, -1, 2, 30, 5);
    }

    protected String getExtensionName() {
        return "Scilab";
    }

    protected void initPASolveConfig(Map<String, Serializable> args) {
        Object conf = args.get("global_config");
        if (conf != null) {
            paconfig = (PASolveScilabGlobalConfig) conf;
        } else {
            paconfig = new PASolveScilabGlobalConfig();
        }

        Object tconf = args.get("task_config");
        if (tconf != null) {
            taskconfig = (PASolveScilabTaskConfig) tconf;
        } else {
            taskconfig = new PASolveScilabTaskConfig();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive.scheduler.common.task.JavaExecutable#init(java.util.Map)
     */
    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        Object s = args.get("script");

        if (s != null) {
            scriptLines = new ArrayList<String>();
            scriptLines.add((String) s);
        }

        URL scriptURL;

        String u = (String) args.get("scriptUrl");

        if (u != null) {
            scriptURL = new URI(u).toURL();

            InputStream is = scriptURL.openStream();
            scriptLines = IOTools.getContentAsList(is);
        }

        String f = (String) args.get("scriptFile");

        if (f != null) {
            FileInputStream fis = new FileInputStream(f);
            scriptLines = IOTools.getContentAsList(fis);
        }

        if (scriptLines.size() == 0) {
            throw new IllegalArgumentException(
                "Either one of \"script\" \"scripturl\" \"scriptfile\" must be given");
        }

        String input = (String) args.get("input");

        if (input != null) {
            inputScript = input;
        }

        String functionsDef = (String) args.get("functionsDefinition");
        if (functionsDef != null) {
            functionsDefinition = functionsDef;
        }

        String funcn = (String) args.get("functionName");
        if (funcn != null) {
            functionName = funcn;
        }

        String outputs = (String) args.get("outputs");
        if (outputs != null) {
            out_set = outputs.split("[ ,]+");
        }

    }

    @Override
    protected void afterExecute() {

    }

    @Override
    public String getWorkerClassName() {
        return AOScilabWorker.class.getName();
    }

    protected void initWorker(W worker) throws Throwable {
        worker.init(inputScript, functionName, functionsDefinition, scriptLines, out_set, paconfig,
                taskconfig, scilabConfig);
    }

    protected void initProcess(MatSciTask.DummyJVMProcess jvmprocess, Map<String, String> env)
            throws Throwable {
        // we add scilab directories to LD_LIBRARY_PATH
        String libPath = env.get("LD_LIBRARY_PATH");
        libPath = addScilabToPath(libPath);

        env.put("LD_LIBRARY_PATH", libPath);

        // we add scilab directories to PATH (Windows)
        String path = env.get("PATH");

        if (path == null) {
            path = env.get("Path");
        }

        env.put("PATH", addScilabToPath(path));

        // This tells scilab to run without graphical interface
        env.put("SCI_JAVA_ENABLE_HEADLESS", "1");
        env.put("SCI_DISABLE_TK", "1");

        // Classpath specific
        String classpath = addScilabJarsToClassPath(javaCommandBuilder.getClasspath());
        javaCommandBuilder.setClasspath(classpath);

        // We add the Scilab specific env variables
        env.put("SCI", scilabConfig.getScilabSCIDir());
        env.put("SCIDIR", scilabConfig.getScilabSCIDir());

        // javaCommandBuilder.setJavaPath(System.getenv("JAVA_HOME") +
        //     "/bin/java");
        // we set as well the java.library.path property (precaution)
        if (CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue().equals("pamr")) {
            String jvmOptions = "-Djava.library.path=\"" + libPath + "\"" +
                " -Dproactive.communication.protocol=" +
                CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue();
            if (PAMRConfig.PA_NET_ROUTER_ADDRESS.isSet()) {
                jvmOptions += " -Dproactive.net.router.address=" +
                    PAMRConfig.PA_NET_ROUTER_ADDRESS.getValue();
            }
            if (PAMRConfig.PA_NET_ROUTER_PORT.isSet()) {
                jvmOptions += " -Dproactive.net.router.port=" + PAMRConfig.PA_NET_ROUTER_PORT.getValue();
            }
            if (PAMRConfig.PA_PAMR_SOCKET_FACTORY.isSet()) {
                jvmOptions += " -Dproactive.communication.pamr.socketfactory=" +
                    PAMRConfig.PA_PAMR_SOCKET_FACTORY.getValue();
            }
            if (PAMRConfig.PA_PAMRSSH_KEY_DIR.isSet()) {
                jvmOptions += " -Dproactive.communication.pamrssh.key_directory=" +
                    PAMRConfig.PA_PAMRSSH_KEY_DIR.getValue();
            }
            if (PAMRConfig.PA_PAMRSSH_REMOTE_USERNAME.isSet()) {
                jvmOptions += " -Dproactive.communication.pamrssh.username=" +
                    PAMRConfig.PA_PAMRSSH_REMOTE_USERNAME.getValue();
            }
            if (PAMRConfig.PA_PAMRSSH_REMOTE_PORT.isSet()) {
                jvmOptions += " -Dproactive.communication.pamrssh.port=" +
                    PAMRConfig.PA_PAMRSSH_REMOTE_PORT.getValue();
            }

            javaCommandBuilder.setJvmOptions(jvmOptions);
        } else {
            javaCommandBuilder.setJvmOptions("-Djava.library.path=\"" + libPath + "\"" +
                " -Dproactive.rmi.port=" + CentralPAPropertyRepository.PA_RMI_PORT.getValue() +
                " -Dproactive.communication.protocol=" +
                CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());
        }
    }

    /**
     * Utility function to add SCILAB jar files to the given path-like string
     *
     * @param classpath path-like string
     * @return an augmented path
     */
    private String addScilabJarsToClassPath(String classpath) {

        String newPath;

        if (classpath == null) {
            newPath = "";
        } else {
            newPath = os.pathSeparator() + classpath;
        }

        newPath = (scilabConfig.getScilabSCIDir() + os.fileSeparator() + "modules" + os.fileSeparator() +
            "jvm" + os.fileSeparator() + "jar" + os.fileSeparator() + "org.scilab.modules.jvm.jar") +
            newPath;

        return newPath;
    }

    /**
     * Utility function to add SCILAB directories to the given path-like string
     *
     * @param path path-like string
     * @return an augmented path
     */
    private String addScilabToPath(String path) {
        String newPath;

        if (path == null) {
            newPath = "";
        } else {
            newPath = os.pathSeparator() + path;
        }

        newPath = (scilabConfig.getScilabHome() + os.fileSeparator() + scilabConfig.getScilabLibDir()) +
            newPath;

        File thirdpartyFolder = new File(scilabConfig.getScilabHome() + os.fileSeparator() +
            scilabConfig.getScilabLibDir() + os.fileSeparator() + ".." + os.fileSeparator() + "thirdparty");
        if (thirdpartyFolder.exists() && (os == OperatingSystem.unix)) {
            newPath = thirdpartyFolder.getPath() + os.pathSeparator() + newPath;
        }

        return newPath;
    }

}