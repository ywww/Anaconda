/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.File;
import java.net.URI;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;

import functionalTests.FunctionalTest;
import functionaltests.executables.ResultAsArray;


/**
 *
 *
 * @author The ProActive Team
 * @date 12 jun 09
 * @since ProActive Scheduling 1.0
 */
public class TestJobInstantGetTaskResult extends FunctionalTest {

    /**
    * Tests start here.
    *
    * @throws Throwable any exception that can be thrown during the test.
    */
    @org.junit.Test
    public void run() throws Throwable {
        //create Scheduler client as an active object
        SubmitJob client = (SubmitJob) PAActiveObject.newActive(SubmitJob.class.getName(), new Object[] {});
        //begin to use the client : must be a futur result in order to start the scheduler at next step
        client.begin();

        //create job
        TaskFlowJob job = new TaskFlowJob();
        try {
            final JobEnvironment je = new JobEnvironment();
            final URI uri = ResultAsArray.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            je.setJobClasspath(new String[] { new File(uri).getAbsolutePath() });
            job.setEnvironment(je);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        for (int i = 0; i < 50; i++) {
            JavaTask t = new JavaTask();
            t.setExecutableClassName(ResultAsArray.class.getName());
            t.setName("task" + i);
            job.addTask(t);
        }

        JobId id = SchedulerTHelper.submitJob(job);
        client.setJobId(id);

        SchedulerTHelper.waitForEventJobRemoved(id);
    }
}