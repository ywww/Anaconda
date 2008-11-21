/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.internal;

import java.io.BufferedReader;
import java.io.FileReader;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.ForkedJavaTaskLauncher;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.ForkEnvironment;
import org.ow2.proactive.scheduler.task.JavaTaskLauncher;
import org.ow2.proactive.scheduler.task.TaskLauncher;


/**
 * Description of a java task.
 * See also @see AbstractJavaTaskDescriptor
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class InternalJavaTask extends InternalTask {

    /** Whether user wants to execute a task in a separate JVM */
    private boolean fork = false;

    /** Environment of a new dedicated JVM */
    private ForkEnvironment forkEnvironment = null;

    /**
     * ProActive empty constructor
     */
    public InternalJavaTask() {
    }

    /**
     * Create a new Java task descriptor using instantiated java task.
     *
     * @param execContainer the Java Executable Container
     */
    public InternalJavaTask(JavaExecutableContainer execContainer) {
        this.executableContainer = new ExecutableContainerDataBaseProxy(execContainer, this);
    }

    /**
     * Create the launcher for this java task Descriptor.
     *
     * @param node the node on which to create the launcher.
     * @return the created launcher as an activeObject.
     * @throws ActiveObjectCreationException If an active object creation failed.
     * @throws NodeException 
     */
    public TaskLauncher createLauncher(Node node) throws ActiveObjectCreationException, NodeException {
        JavaTaskLauncher launcher = null;
        if (fork || isWallTime()) {
            String forkedPolicycontent = getJavaPolicy();
            if (getPreScript() == null && getPostScript() == null) {
                launcher = (ForkedJavaTaskLauncher) PAActiveObject.newActive(ForkedJavaTaskLauncher.class
                        .getName(), new Object[] { getId(), forkedPolicycontent }, node);
            } else {
                launcher = (ForkedJavaTaskLauncher) PAActiveObject.newActive(ForkedJavaTaskLauncher.class
                        .getName(), new Object[] { getId(), forkedPolicycontent, getPreScript(),
                        getPostScript() }, node);
            }
            ((ForkedJavaTaskLauncher) launcher).setForkEnvironment(forkEnvironment);
        } else {
            if (getPreScript() == null && getPostScript() == null) {
                launcher = (JavaTaskLauncher) PAActiveObject.newActive(JavaTaskLauncher.class.getName(),
                        new Object[] { getId() }, node);
            } else {
                launcher = (JavaTaskLauncher) PAActiveObject.newActive(JavaTaskLauncher.class.getName(),
                        new Object[] { getId(), getPreScript(), getPostScript() }, node);
            }
        }
        setExecuterInformations(new ExecuterInformations(launcher, node));
        setKillTaskTimer(launcher);

        return launcher;
    }

    /**
     * Return the content of the forked java policy or a default one if not found.
     *
     * @return the content of the forked java policy or a default one if not found.
     */
    private String getJavaPolicy() {
        StringBuilder content;
        try {
            content = new StringBuilder("");
            String forkedPolicyFilePath = PASchedulerProperties
                    .getAbsolutePath(PASchedulerProperties.SCHEDULER_DEFAULT_FJT_SECURITY_POLICY
                            .getValueAsString());
            BufferedReader brin = new BufferedReader(new FileReader(forkedPolicyFilePath));
            String line;
            while ((line = brin.readLine()) != null) {
                content.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            content = new StringBuilder("grant {\npermission java.security.AllPermission;\n};\n");
        }
        return content.toString();
    }

    /**
     * @return the fork if the user wants to execute the task in a separate JVM
     */
    public boolean isFork() {
        return fork;
    }

    /**
     * @param fork if the user wants to execute the task in a separate JVM
     */
    public void setFork(boolean fork) {
        this.fork = fork;
    }

    /**
     * @return the forkEnvironment
     */
    public ForkEnvironment getForkEnvironment() {
        return forkEnvironment;
    }

    /**
     * @param forkEnvironment the forkEnvironment to set
     */
    public void setForkEnvironment(ForkEnvironment forkEnvironment) {
        this.forkEnvironment = forkEnvironment;
    }

}
