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
package org.objectweb.proactive.extra.scheduler.common.exception;


/**
 * Exceptions Generated if a problem occurred while creating a job.
 *
 * @author jlscheef - ProActive Team
 * @version 1.0, Jun 29, 2007
 * @since ProActive 3.2
 */
public class JobCreationException extends Exception {

    /** Serial version UID */
    private static final long serialVersionUID = -3426241467389188582L;

    /**
     * Create a new instance of JobCreationException with the given message and cause
     *
     * @param msg the message to attach.
     * @param cause the cause of the exception.
     */
    public JobCreationException(String string, Exception e) {
        super(string, e);
    }
}
