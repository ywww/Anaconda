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
package org.ow2.proactive.scheduler.common;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Constant types in the Scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 *
 */
@PublicAPI
public class SchedulerConstants {

    /** Default scheduler node name */
    public static final String SCHEDULER_DEFAULT_NAME = "SCHEDULER";

    /** Default job name */
    public static final String JOB_DEFAULT_NAME = "NOT SET";

    /** Default task name */
    public static final String TASK_DEFAULT_NAME = "NOT SET";

    /** If the task name is not set, this is the generated one */
    public static final String TASK_NAME_IFNOTSET = "task_";

    /** Name of the environment variable for windows home directory on the common file system. */
    public static final String WINDOWS_HOME_ENV_VAR = "WINDOWS_HOME";

    /** Name of the environment variable for unix home directory on the common file system. */
    public static final String UNIX_HOME_ENV_VAR = "UNIX_HOME";

    /** Name of the GlobalSpace for DataSpaces registration */
    public static final String GLOBALSPACE_NAME = "GLOBALSPACE";

    /**
     * Marker in the task output to locate the remote connection hint
     *  
     * for the hint to be detected client side, the following String has 
     * to be printed to the task log :
     * 
     *  PA_REMOTE_CONNECTION;TaskId;type;url
     *  
     *  example : 'PA_REMOTE_CONNECTION;10005;vnc;localhost:5901'
     */
    public static final String REMOTE_CONNECTION_MARKER = "PA_REMOTE_CONNECTION";

    /** Separator character for the String located by the {@link #REMOTE_CONNECTION_MARKER} */
    public static final char REMOTE_CONNECTION_SEPARATOR = ';';
}
