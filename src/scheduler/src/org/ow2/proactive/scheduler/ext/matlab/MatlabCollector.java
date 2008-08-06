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
package org.ow2.proactive.scheduler.ext.matlab;

import java.io.Serializable;

import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.scheduler.common.task.TaskResult;


public class MatlabCollector extends SimpleMatlab {
    private static AOMatlabCollector collectorWorker = null;

    /**
     *
     */
    public MatlabCollector() {
    }

    @Override
    protected Serializable executeInternal(String uri, TaskResult... results) throws Throwable {

        if (collectorWorker == null) {
            if (debug) {
                logger.info("[" + host + " MATLAB TASK] Deploying Worker (MatlabCollector)");
            }
            collectorWorker = (AOMatlabCollector) deploy(uri, AOMatlabCollector.class.getName(), matlabConfig
                    .getMatlabCommandName());
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    collectorWorker.terminate();
                }
            }));
        }

        collectorWorker.init(inputScript, scriptLines);
        if (debug) {
            logger.info("[" + host + " MATLAB TASK] Executing (Collector)");
        }

        Serializable res = collectorWorker.execute(results);
        res = (Serializable) PAFuture.getFutureValue(res);
        // We don't terminate the worker for subsequent calculations
        // collectorWorker.terminate();

        return res;
    }
}
