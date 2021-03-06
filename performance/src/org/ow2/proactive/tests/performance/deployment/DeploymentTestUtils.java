/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.deployment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.tests.performance.deployment.process.ProcessExecutor;
import org.ow2.proactive.tests.performance.deployment.process.SSHProcessExecutor;
import org.ow2.proactive.tests.performance.utils.FindFreePort;


public class DeploymentTestUtils {

    static final String PGREP_COMMAND = "pgrep";

    static final String PS_COMMAND = "ps";

    static final String KILL_COMMAND = "kill";

    static final String DIR_TEST_COMMAND = "cd";

    public static InetAddress checkHostIsAvailable(String hostName) {
        try {
            InetAddress hostAddr = InetAddress.getByName(hostName.trim());
            /*
            if (!hostAddr.isReachable(30000)) {
                System.out.println("ERROR: InetAddress.isReachable is false for " + hostName);
                return null;
            }
             */
            return hostAddr;
        } catch (UnknownHostException e) {
            System.out.println("Unknow host error for host " + hostName + ": " + e);
            return null;
        }
    }

    public static Integer findFreePort(HostTestEnv env) throws InterruptedException {
        List<String> command = new ArrayList<String>();
        command.add(env.getEnv().getJavaPath());
        command.add("-cp");
        command.add(env.getEnv().getSchedulingFolder().getPerformanceClassesDir().getAbsolutePath());
        command.add(FindFreePort.class.getName());
        ProcessExecutor executor = env.runCommandSaveOutput("findPort", command);
        if (!executor.executeAndWaitCompletion(10000, true)) {
            throw new TestExecutionException("Failed to execute command to find free port");
        }
        List<String> output = executor.getOutput();
        if (output.isEmpty()) {
            throw new TestExecutionException("Empty output for command finding free port. Error output: " +
                executor.getErrorOutput());
        }
        try {
            return Integer.valueOf(output.get(0));
        } catch (NumberFormatException e) {
            throw new TestExecutionException("Invalid port was detected", e);
        }
    }

    public static boolean isJavaIsAvailable(InetAddress hostAddr, String javaPath)
            throws InterruptedException {
        SSHProcessExecutor executor = SSHProcessExecutor.createExecutorSaveOutput("java -version", hostAddr,
                javaPath, "-version");
        return executor.executeAndWaitCompletion(10000, true);
    }

    public static void checkJavaIsAvailable(InetAddress hostAddr, String javaPath) {
        try {
            if (!isJavaIsAvailable(hostAddr, javaPath)) {
                throw new TestExecutionException("Java '" + javaPath + "' isn't available at " + hostAddr);
            }
        } catch (TestExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new TestExecutionException("Unexpected exception: " + e, e);
        }
    }

    public static boolean isPathIsAvailable(InetAddress hostAddr, String path) throws InterruptedException {
        SSHProcessExecutor executor = SSHProcessExecutor.createExecutorSaveOutput(DIR_TEST_COMMAND, hostAddr,
                DIR_TEST_COMMAND, path);
        return executor.executeAndWaitCompletion(10000, true);
    }

    public static void checkPathIsAvailable(InetAddress hostAddr, String path) {
        try {
            if (!isPathIsAvailable(hostAddr, path)) {
                throw new TestExecutionException("Path '" + path + "' isn't available at " + hostAddr);
            }
        } catch (TestExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new TestExecutionException("Unexpected exception: " + e, e);
        }
    }

    public static void killTestProcesses(Collection<String> hosts, String stringToFind) {
        for (String hostName : hosts) {
            InetAddress hostAddr = checkHostIsAvailable(hostName);
            if (hostAddr != null) {
                try {
                    killProcessesUsingPgrep(hostAddr, stringToFind);
                } catch (InterruptedException e) {
                    System.out.println("Unexpected exception: " + e);
                }
            }
        }
    }

    public static List<String> listProcesses(InetAddress hostAddr, String stringToFind)
            throws InterruptedException {
        SSHProcessExecutor ps = SSHProcessExecutor.createExecutorSaveOutput(PS_COMMAND, hostAddr, PS_COMMAND,
                "-ef");
        if (!ps.executeAndWaitCompletion(10000, true)) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        for (String line : ps.getOutput()) {
            if (line.toLowerCase().contains(stringToFind)) {
                result.add(line);
            }
        }
        return result;
    }

    public static boolean killProcessesUsingPgrep(InetAddress hostAddr, String stringToFind)
            throws InterruptedException {
        SSHProcessExecutor grep = SSHProcessExecutor.createExecutorSaveOutput(PGREP_COMMAND, hostAddr,
                PGREP_COMMAND, "-f", stringToFind);
        grep.executeAndWaitCompletion(10000, false);

        List<String> output = grep.getOutput();
        if (output.isEmpty()) {
            return true;
        }

        List<String> killCommand = new ArrayList<String>();
        killCommand.add(KILL_COMMAND);
        killCommand.add("-9");
        for (String outputLine : grep.getOutput()) {
            try {
                Integer pid = Integer.valueOf(outputLine);
                killCommand.add(pid.toString());
            } catch (NumberFormatException e) {
                System.out.println("Invalid PID was detected: " + outputLine);
            }
        }
        SSHProcessExecutor kill = SSHProcessExecutor.createExecutorSaveOutput(KILL_COMMAND, hostAddr,
                killCommand.toArray(new String[killCommand.size()]));
        return kill.executeAndWaitCompletion(10000, true);
    }

    public static String createProActiveConfiguration(Map<String, String> properties) {
        StringBuilder result = new StringBuilder();
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        result.append("<ProActiveUserProperties>\n");
        result.append("<properties>\n");
        for (Map.Entry<String, String> property : properties.entrySet()) {
            result.append(String.format("    <prop key=\"%s\" value=\"%s\"/>\n", property.getKey(), property
                    .getValue()));
        }
        result.append("</properties>\n");
        result.append("</ProActiveUserProperties>\n");
        return result.toString();
    }

}
