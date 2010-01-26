/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.common;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Enumeration of all states of a RMNode :<BR>
 * -free : node is ready to perform a task.<BR>
 * -busy : node is executing a task.<BR>
 * -to be released : node is busy and have to be removed at the end of its current task.<BR>
 * -down : node is broken, and not anymore able to perform tasks.<BR>
 *
 * @see org.ow2.proactive.resourcemanager.rmnode.RMNode
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
@PublicAPI
public enum NodeState {
    /**
     * a node that can be provided to a RM user, and able to perform a task
     */
    FREE("Free"),
    /**
     * a node provided to a RM user.
     */
    BUSY("Busy"),
    /**
     * a node that has been detected down.
     */
    DOWN("Down"),
    /**
     * a node busy which must be removed from Resource manager, when the RM user
     * will give back the node.
     * 
     */
    TO_BE_RELEASED("To be Released");

    private String desc;

    /**
     * Constructor
     * @param nb state to specify.
     */
    NodeState(String desc) {
        this.desc = desc;
    }

    /**
     * Gives a string representation of the state.
     * @return String representation of the state.
     */
    @Override
    public String toString() {
        return desc;
    }
}
