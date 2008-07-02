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
package org.ow2.proactive.scheduler.resourcemanager;

import java.util.Vector;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.*;


/**This is the interface that should be implemented regarding any resource manager submitted to the scheduler.
 *
 * @author The ProActive Team
 *
 */
public interface GenericResourceManager {
    /**
     * Non-blocking function that returns a maximum of NB nodes if available it can also return zero.<br/>
     * Reserves as many nodes as possible upto maxNodeNb and returns them as an arraylist, if nothing is possbile or argument is zero return an empty array
     * 
     * @param maxNodeNb the max number of node to ask.
     * @return a maximum of NB nodes if available
     *
     */
    Vector<Node> getAtMostNNodes(IntWrapper maxNodeNb);

    /**
     * Returns the nodes to the resource manager
     * </br><b>It is highly advisable to kill the object on the nodes upon arrival</b>
     * 
     * @param nodes of nodes to be returned
     */
    public void freeNodes(Vector<Node> nodes);
}
