package jobsubmission;

import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerConnection;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEvent;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * This class tests a basic actions of a job submission to ProActive scheduler :
 * Connection to scheduler, with authentication
 * Register a monitor to Scheduler in order to receive events concerning
 * job submission.
 * 
 * Submit a job (test 1). 
 * After the job submission, the test monitor all jobs states changes, in order
 * to observe its execution :
 * job submitted (test 2),
 * job pending to running (test 3),
 * all task pending to running, and all tasks running to to finished (test 4),
 * job running to finished (test 5).
 * After it retrieves job's result and check that all 
 * tasks results are available (test 6).
 * 
 * @author The ProActive Team
 * @date 2 jun 08
 * @version 4.0
 * @since ProActive 4.0
 */
public class TestJobSubmission extends FunctionalTDefaultScheduler {

    private static String jobDescriptor = TestJobSubmission.class.getResource("/jobsubmission/Job_PI.xml")
            .getPath();

    private String username = "jl";
    private String password = "jl";

    private SchedulerEventReceiver receiver = null;

    /**
     *  Starting and linking new scheduler ! <br/>
     *  This method will join a new scheduler and connect it as user.<br/>
     *  Then, it will register an event receiver to check the dispatched event.
     */
    @Before
    public void preRun() throws Exception {
        System.out.println("------------------------------ Starting and linking new scheduler !...");
        //join the scheduler
        schedulerAuth = SchedulerConnection.join(null);
        //Log as user
        schedUserInterface = schedulerAuth.logAsUser(username, password);
        //Create an Event receiver AO in order to observe jobs and tasks states changes
        receiver = (SchedulerEventReceiver) PAActiveObject.newActive(SchedulerEventReceiver.class.getName(),
                new Object[] {});
        //Register as EventListener AO previously created
        schedUserInterface.addSchedulerEventListener(receiver, SchedulerEvent.JOB_SUBMITTED,
                SchedulerEvent.JOB_PENDING_TO_RUNNING, SchedulerEvent.JOB_RUNNING_TO_FINISHED,
                SchedulerEvent.JOB_REMOVE_FINISHED, SchedulerEvent.TASK_PENDING_TO_RUNNING,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED);
    }

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        System.out.println("------------------------------ Test 1 : Submitting job...");
        //job creation
        Job submittedJob = JobFactory.getFactory().createJob(jobDescriptor);
        //job submission
        JobId id = schedUserInterface.submit(submittedJob);

        System.out.println("------------------------------ Test 2 : Verifying submission...");
        // wait for event : job submitted
        receiver.waitForNEvent(1);
        ArrayList<Job> jobsList = receiver.cleanNgetJobSubmittedEvents();
        assertTrue(jobsList.size() == 1);
        Job job = jobsList.get(0);
        assertTrue(job.getId().equals(id));

        System.out.println("------------------------------ Test 3 : Verifying start of job execution...");
        //wait for event : job pending to running
        receiver.waitForNEvent(1);
        ArrayList<JobEvent> eventsList = receiver.cleanNgetJobPendingToRunningEvents();
        assertTrue(eventsList.size() == 1);
        JobEvent jEvent = eventsList.get(0);
        assertTrue(jEvent.getJobId().equals(id));

        System.out.println("------------------------------ Test 4 : Verifying start of each tasks...");
        //wait whole tasks execution : two events per task, task pending to running, and task running to finished  
        receiver.waitForNEvent(jEvent.getTotalNumberOfTasks() * 2);
        ArrayList<TaskEvent> tEventList = receiver.cleanNgetTaskPendingToRunningEvents();
        assertTrue(tEventList.size() == jEvent.getTotalNumberOfTasks());
        tEventList = receiver.cleanNgetTaskRunningToFinishedEvents();
        assertTrue(tEventList.size() == jEvent.getTotalNumberOfTasks());

        System.out.println("------------------------------ Test 5 : Verifying job termination...");
        //wait for event : job Running to finished
        receiver.waitForNEvent(1);
        eventsList = receiver.cleanNgetjobRunningToFinishedEvents();
        assertTrue(eventsList.size() == 1);
        jEvent = eventsList.get(0);
        assertTrue(jEvent.getJobId().equals(id));

        System.out.println("------------------------------ Test 6 : Getting job result...");
        JobResult res = schedUserInterface.getJobResult(id);
        schedUserInterface.remove(id);
        //check that there is no exception in results
        assertTrue(res.getExceptionResults().size() == 0);
        //wait for event : result retrieval
        receiver.waitForNEvent(1);
        eventsList = receiver.cleanNgetjobRemoveFinishedEvents();
        assertTrue(eventsList.size() == 1);
        jEvent = eventsList.get(0);
        assertTrue(jEvent.getJobId().equals(id));
        HashMap<String, TaskResult> results = res.getAllResults();
        //check that number of results correspond to number of tasks       
        assertTrue(jEvent.getNumberOfFinishedTasks() == results.size());
        //check that all tasks results are defined
        for (TaskResult taskRes : results.values()) {
            assertTrue(taskRes.value() != null);
        }
    }

    /**
     * Disconnect the scheduler.
     *
     * @throws Exception if an error occurred
     */
    @After
    public void afterTestJobSubmission() throws Exception {
        System.out.println("------------------------------ Disconnecting from scheduler...");
        schedUserInterface.disconnect();
    }

}
