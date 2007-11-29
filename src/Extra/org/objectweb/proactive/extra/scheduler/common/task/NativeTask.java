/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extra.scheduler.common.task;

import org.objectweb.proactive.extra.scheduler.common.job.TaskFlowJob;
import org.objectweb.proactive.extra.scheduler.common.scripting.GenerationScript;


/**
 * Use this class to build a native task that will use a {@link NativeExecutable} and be integrated in a {@link TaskFlowJob}.<br>
 * A native task just includes a command line that can be set using {@link #setCommandLine(String)}.<br>
 * You don't have to extend this class to launch your own native executable.
 *
 * @author jlscheef - ProActiveTeam
 * @version 1.0, Sept 14, 2007
 * @since ProActive 3.2
 * @publicAPI
 */
public class NativeTask extends Task {

    /** Serial version UID */
    private static final long serialVersionUID = -2327189450547547292L;

    /** Command line for this native task */
    private String commandLine = null;

    /** GenerationScript if this command is generated by a script */
    private GenerationScript gscript = null;

    /**
     * Empty constructor.
     */
    public NativeTask() {
    }

    /**
     * @return the command line
     */
    public String getCommandLine() {
        return commandLine;
    }

    /**
     * @return the generation script
     */
    public GenerationScript getGenerationScript() {
        return gscript;
    }

    /**
     * @param commandLine the commandLine to set
     */
    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public void setGenerationScript(GenerationScript gscript) {
        this.gscript = gscript;
    }
}
