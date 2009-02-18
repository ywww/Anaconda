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
package org.ow2.proactive.scheduler.common;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * Scheduler interface for someone connected to the scheduler as user.<br>
 * This interface provides methods to managed the user task and jobs on the scheduler.<br>
 * A user will only be able to managed his jobs and tasks, and also see the entire scheduling process.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface UserSchedulerInterface extends UserSchedulerInterface_ {

    /**
     * Submit a new job to the scheduler.
     * A user can only managed their jobs.
     * <p>
     * It will execute the tasks of the jobs as soon as resources are available.
     * The job will be considered as finished once every tasks have finished.
     * </p>
     * Thus, user could get the job result according to the precious result.
     * <br /><br />
     * It is possible to get a listener on the scheduler. (see {@link UserSchedulerInterface.addSchedulerEventListener()} for more details)
     *
     * @param job the new job to submit.
     * @return the generated new job ID.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public JobId submit(Job job) throws SchedulerException;

    /**
     * Get the result for the given jobId.<br>
     * The jobId is given as a string. It's in fact the string returned by the {@link JobId.value()} method.<br>
     * A user can only get HIS result back except if he is admin.<br>
     * If the job does not exist, a schedulerException is sent with the proper message.<br>
     * So, if you have the right to get the job result represented by the given jobId and if the job exists,
     * so you will receive the result. In any other cases a schedulerException will be thrown.
     *
     * @param jobId the job on which the result will be send
     * @return a job Result containing information about the result.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public JobResult getJobResult(String jobId) throws SchedulerException;

    /**
     * Get the result for the given task name in the given jobId. <br />
     * The jobId is given as a string. It's in fact the string returned by the {@link JobId.value()} method.<br>
     * A user can only get HIS result back.<br>
     * If the job does not exist, a schedulerException is sent with the proper message.<br>
     * So, if you have the right to get the task result that is in the job represented by the given jobId and if the job and task name exist,
     * so you will receive the result. In any other cases a schedulerException will be thrown.<br>
     *
     * @param jobId the job in which the task result is.
     * @param taskName the name of the task in which the result is.
     * @return a job Result containing information about the result.
     * 		If null is returned, this task is not yet terminated.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public TaskResult getTaskResult(String jobId, String taskName) throws SchedulerException;

    /**
     * Add a scheduler event Listener. this listener provides method to notice of
     * new coming job, started task, finished task, running job, finished job, etc...<br>
     * You may use this method once by remote or active object.<br>
     * Every call to this method will remove your previous listening settings.<br>
     * If you want to get 2 events, add the 2 events you want at the end of this method. If no events are specified, all of them
     * will be sent.
     *
     * @param sel a SchedulerEventListener on which the scheduler will talk.
     * @param events An array of events that you want to receive from the scheduler.
     * @return the scheduler current state containing the different lists of jobs.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right), or if the registered listener is not a remote object.
     */
    public SchedulerInitialState<? extends Job> addSchedulerEventListener(
            SchedulerEventListener<? extends Job> sel, SchedulerEvent... events) throws SchedulerException;

    /**
     * Remove the current scheduler event listener your listening on.<br>
     * If no listener is defined, this method has no effect.
     */
    public void removeSchedulerEventListener() throws SchedulerException;

    /**
     * Return the scheduler statistics.<br>
     * It will be possible to get an HashMap of all properties set in the stats class.
     *
     * @return the scheduler statistics.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public Stats getStats() throws SchedulerException;

    /**
     * Disconnect properly the user from the scheduler.
     *
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public void disconnect() throws SchedulerException;

    /**
     * Test whether or not the user is connected to a ProActive Scheduler.
     *
     * @return true if the user connected to a Scheduler, false otherwise.
     */
    public BooleanWrapper isConnected();
}