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
package org.ow2.proactive.scheduler.ext.scilab.util;

import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.scilab.AOScilabWorker;
import org.objectweb.proactive.core.node.Node;


/**
 * ScilabJVMInfo
 *
 * @author The ProActive Team
 */
public class ScilabJVMInfo {
    IOTools.LoggingThread esLogger = null;

    IOTools.LoggingThread isLogger = null;

    IOTools.RedirectionThread ioThread = null;

    AOScilabWorker worker = null;

    Node node = null;

    Integer deployID = null;

    Process process = null;

    public ScilabJVMInfo() {

    }

    public ScilabJVMInfo(IOTools.LoggingThread esLogger, IOTools.LoggingThread isLogger,
            IOTools.RedirectionThread ioThread, AOScilabWorker worker, Node node, Integer deployID,
            Process process) {
        this.esLogger = esLogger;
        this.isLogger = isLogger;
        this.ioThread = ioThread;
        this.worker = worker;
        this.node = node;
        this.deployID = deployID;
        this.process = process;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public IOTools.LoggingThread getEsLogger() {
        return esLogger;
    }

    public void setEsLogger(IOTools.LoggingThread esLogger) {
        this.esLogger = esLogger;
    }

    public IOTools.LoggingThread getIsLogger() {
        return isLogger;
    }

    public void setIsLogger(IOTools.LoggingThread isLogger) {
        this.isLogger = isLogger;
    }

    public IOTools.RedirectionThread getIoThread() {
        return ioThread;
    }

    public void setIoThread(IOTools.RedirectionThread ioThread) {
        this.ioThread = ioThread;
    }

    public AOScilabWorker getWorker() {
        return worker;
    }

    public void setWorker(AOScilabWorker worker) {
        this.worker = worker;
    }

    public Integer getDeployID() {
        return deployID;
    }

    public void setDeployID(Integer deployID) {
        this.deployID = deployID;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }
}
