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
package org.ow2.proactive.scheduler.ext.matlab;

import java.io.Serializable;

import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * A Scheduler Task, specific to Matlab, which handles gathering of results in matlab workflows
 *
 * @author The ProActive Team
 */
public class MatlabCollector extends MatlabTask {
    private static AOMatlabCollector collectorWorker = null;

    /**
     *
     */
    public MatlabCollector() {
    }

    @Override
    protected Serializable executeInternal(TaskResult... results) throws Throwable {

        if (collectorWorker == null) {
            if (debug) {
                System.out.println("[" + host + " MATLAB TASK] Deploying Worker (MatlabCollector)");
            }
            collectorWorker = (AOMatlabCollector) deploy(AOMatlabCollector.class.getName());
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    collectorWorker.terminate();
                }
            }));
        }

        collectorWorker.init(inputScript, scriptLines, debug);
        if (debug) {
            System.out.println("[" + host + " MATLAB TASK] Executing (Collector)");
        }

        Serializable res = collectorWorker.execute(results);
        res = (Serializable) PAFuture.getFutureValue(res);
        // We don't terminate the worker for subsequent calculations
        // collectorWorker.terminate();

        return res;
    }
}
