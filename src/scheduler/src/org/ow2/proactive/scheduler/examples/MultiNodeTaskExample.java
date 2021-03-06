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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.examples;

import java.io.Serializable;
import java.security.KeyException;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.topology.descriptor.ThresholdProximityDescriptor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;


/**
 * The class created a simple multi-nodes task that finds Nth prime of the number in parallel.
 * We demonstrate how it is possible to specify topology parameter for such task.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
public class MultiNodeTaskExample {
    /**
     * Start the example.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            //*********************** GET SCHEDULER *************************
            //get authentication interface from existing scheduler based on scheduler host URL
            //(localhost) followed by the scheduler name (here the default one)
            SchedulerAuthenticationInterface auth = SchedulerConnection.join("//localhost/");

            //Now you are connected you must log on with a couple of username/password matching an entry in login and group files.
            //(groups.cfg, login.cfg in the same directory)
            //you can also log on as admin if your username is in admin group. (it provides you more power ;) )
            //your need to create encrypted credentials so that it cannot be intercepted by network sniffers;
            //the scheduler will offer the public key required for encryption
            Credentials cred = Credentials
                    .createCredentials(new CredData("user", "pwd"), auth.getPublicKey());
            Scheduler scheduler = auth.login(cred);

            //if this point is reached, that's we are connected to the scheduler under "user".
            //******************** CREATE A NEW JOB ***********************
            //params are respectively : name, priority,cancelOnError, description.
            TaskFlowJob job = new TaskFlowJob();
            job.setName("job name");
            job.setPriority(JobPriority.NORMAL);
            job.setCancelJobOnError(false);
            job.setDescription("Job with multi-node task");
            //******************** CREATE A MULTI-NODE TASK ***********************

            //Create the java task
            JavaTask task = new JavaTask();
            task.setName("Nth prime");
            //adding the task to the job
            task.setExecutableClassName(MultiNodeExample.class.getName());
            //this task is final, it means that the job result will contain this task result.
            task.setPreciousResult(true);

            // We're demonstrating all possible topologies.
            // The last one will be used in the example, so the example
            // will work only when you have 5 hosts in the resource manager

            //@snippet-start java_task_multinodes_java_api
            // setting up the parallel environment with best proximity descriptor
            task.setParallelEnvironment(new ParallelEnvironment(5, TopologyDescriptor.BEST_PROXIMITY));
            // or the parallel environment with 100 microseconds threshold proximity
            task.setParallelEnvironment(new ParallelEnvironment(5, new ThresholdProximityDescriptor(100)));
            // or 5 nodes all on the same host
            task.setParallelEnvironment(new ParallelEnvironment(5, TopologyDescriptor.SINGLE_HOST));
            // or 5 nodes all on the same host exclusively (on other tasks will be executed there in the same time)
            task.setParallelEnvironment(new ParallelEnvironment(5, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE));
            // or 5 nodes from several hosts exclusively (on other tasks will be executed there in the same time)
            task.setParallelEnvironment(new ParallelEnvironment(5,
                TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE));
            // or 5 hosts (1 node from each host exclusively)
            task.setParallelEnvironment(new ParallelEnvironment(5,
                TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE));
            //@snippet-end java_task_multinodes_java_api

            //add the task to the job
            try {
                job.addTask(task);
            } catch (UserException e2) {
                e2.printStackTrace();
            }

            //******************** SUBMIT THE JOB ***********************
            //submitting a job to the scheduler returns the attributed jobId
            //this id will be used to talk the scheduler about this job.
            System.out.println("Submitting job...");
            JobId jobId = scheduler.submit(job);

            //******************** GET JOB OUTPUT ***********************
            System.out.println("Getting job output...");
            try {
                // it will launch a listener that will listen connection on any free port
                LogForwardingService lfs = new LogForwardingService(
                    "org.ow2.proactive.scheduler.common.util.logforwarder.providers.SocketBasedForwardingProvider");
                lfs.initialize();
                // next, this method will forward task output on the previous loggerServer
                scheduler.listenJobLogs(jobId, lfs.getAppenderProvider());
            } catch (LogForwardingException e) {
                e.printStackTrace();
            }

            //******************** GET JOB RESULT ***********************
            // it is better to get the result when the job is terminated.
            // if you want the result as soon as possible we suggest this loop.
            // In the future you could get the result like a future in ProActive or with a listener.
            JobResult result = null;

            while (result == null) {
                try {
                    Thread.sleep(2000);
                    result = scheduler.getJobResult(jobId);

                    //the result is null if the job is not finished.
                } catch (SchedulerException se) {
                    se.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            result.getPreciousResults().get("toto");
            System.out.println("Result : " + result);
        } catch (SchedulerException e) {
            //the scheduler had a problem
            e.printStackTrace();
        } catch (LoginException e) {
            //there was a problem during scheduler authentication
            e.printStackTrace();
        } catch (KeyException e) {
            //credentials could not be encrypted
            e.printStackTrace();
        }
        System.exit(0);
    }

    private class InternalExec extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) {
            System.out.println("Hello World !");

            return "HelloWorld Sample host : " + ProActiveInet.getInstance().getHostname();
        }
    };
}
