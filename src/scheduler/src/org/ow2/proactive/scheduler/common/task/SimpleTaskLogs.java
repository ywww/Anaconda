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
package org.ow2.proactive.scheduler.common.task;

/**
 * A simple String based implementation of TaskLogs.
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SimpleTaskLogs implements TaskLogs {
    // logs on standard output
    private final String standardLogs;

    // logs on error output
    private final String errorlogs;

    /**
     * Create a new SimpleTaskLogs.
     * @param stdLogs the standard output.
     * @param errLogs the error output.
     */
    public SimpleTaskLogs(String stdLogs, String errLogs) {
        this.standardLogs = stdLogs;
        this.errorlogs = errLogs;
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getAllLogs(boolean)
     */
    public String getAllLogs(boolean timeStamp) {
        return this.standardLogs + this.errorlogs;
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStderrLogs(boolean)
     */
    public String getStderrLogs(boolean timeStamp) {
        return this.errorlogs;
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStdoutLogs(boolean)
     */
    public String getStdoutLogs(boolean timeStamp) {
        return this.standardLogs;
    }
}
