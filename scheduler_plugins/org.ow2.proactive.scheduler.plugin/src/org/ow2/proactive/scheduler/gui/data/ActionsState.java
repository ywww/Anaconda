package org.ow2.proactive.scheduler.gui.data;

import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerState;


/**
 * 
 *
 * @author The ProActive Team
 */
public class ActionsState {

    private SchedulerState schedulerState;
    private JobState jobState;
    private boolean admin;
    private boolean connected;
    private boolean jobInFinishQueue;
    private boolean jobSelected;
    private boolean owner;
}