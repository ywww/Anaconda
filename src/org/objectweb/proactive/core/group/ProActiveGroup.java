/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*  
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*  
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s): 
* 
* ################################################################
*/ 
package org.objectweb.proactive.core.group;


import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfProxyObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.InvalidProxyClassException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;



/**
 * This class provides static methods to manage objects representing a Group (<b>typed group</b>).<br><br>
 * 
 * The ProActiveGroup class provides a set of static services through static method calls.
 * It is the main entry point for users of ProActive Group Communication as they will call methods of this class
 * to create group of object or to synchronize them.<br><br>
 * 
 * The main role of ProActiveGroup is to provide methods to create typed group. It is possible to create a typed
 * group (empty or not) through instantiation using one of the version of newActive.
 * It is also possible to create an active typed group from an existing using using the turnActive methods.<br>
 * The default behavior is a broadcast call, with a unique serialization of parameters.<br><br>
 * <b>Warning !!!</b> When a typed group is turned active, it looses the ability to switch to the
 * <code>Group</code> representation. So an active typed group acquire the abilites of any active object
 * (remote reference, migration, ...) but is no more able to evolve : no modification of the membership is possible.<br>
 * (This feature will may appear in a later version of ProActive group communication). 
 *   
 * @author Laurent Baduel
 *
 */
public class ProActiveGroup {

	/** The name of the default proxy for group communication */
	public static final Class DEFAULT_PROXYFORGROUP_CLASS = org.objectweb.proactive.core.group.ProxyForGroup.class;

	/** The name of the default proxy for group communication */
    public static final String DEFAULT_PROXYFORGROUP_CLASS_NAME = "org.objectweb.proactive.core.group.ProxyForGroup";


	private ProActiveGroup () {}


    /**
     * Returns a Group corresponding to the Object o representing a group. Return null if o does not representing a group.
     * @param <code>o</code> - the object representing a group (a typed group).
     * @return the Group corresponding to <code>o</code>.
     */
    public static Group getGroup(Object o) {
		return findProxyForGroup(o);
    }
    
    /**
     * Returns the name class of the typed group.
     * If the parameter is not a typed group, returns the name of Class of the parameter.
     * @param <code>o</code> the typed group for wich we want the name of the type (Class).
     * @return the name class of the typed group 
     */
	public static String getType (Object o) {
		ProxyForGroup tmp = findProxyForGroup(o);
		if (tmp != null)
			return tmp.getTypeName();
		else
			return o.getClass().getName();
	}


    /**
     * Creates an object representing an empty group specifying the upper class of member.
     * @param the name of the (upper) class of the group's member. 
     * @return an empty group of type <code>className</code>.  
     * @throws ClassNotFoundException if the Class corresponding to <code>className</code> can't be found.
     * @throws ClassNotReifiableException if the Class corresponding to <code>className</code> can't be reify.
     */
    public static Object newGroup(String className) throws ClassNotFoundException, ClassNotReifiableException {
	
	MOP.checkClassIsReifiable(MOP.forName(className));
	
	Object result = null;
	
	try {
	    result = MOP.newInstance (className, null, DEFAULT_PROXYFORGROUP_CLASS_NAME, null);
 	}
 	catch (ClassNotReifiableException e) { System.err.println("**** ClassNotReifiableException ****"); }
 	catch (InvalidProxyClassException e) { System.err.println("**** InvalidProxyClassException ****"); }
 	catch (ConstructionOfProxyObjectFailedException e) { System.err.println("**** ConstructionOfProxyObjectFailedException ****"); }
 	catch (ConstructionOfReifiedObjectFailedException e) { System.err.println("**** ConstructionOfReifiedObjectFailedException ****"); }

	ProxyForGroup proxy = (org.objectweb.proactive.core.group.ProxyForGroup)((StubObject)result).getProxy();
	proxy.className = className;
	proxy.stub = (StubObject)result;

	return result;
    }



    /**
     * Creates an object representing a group (a typed group) and creates members with params cycling on nodeList.
     * @param <code>className</code> the name of the (upper) class of the group's member.
     * @param <code>params</code> the array that contain the parameters used to build the group's member.
     * @param <code>nodeList</code> the nodes where the members are created.
     * @return a typed group with its members.
     * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @throws ClassNotFoundException if the Class corresponding to <code>className</code> can't be found.
     * @throws ClassNotReifiableException if the Class corresponding to <code>className</code> can't be reify.
     * @throws NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Object newGroup(String className, Object[][] params, Node[] nodeList)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {

	Object result = newGroup(className);
	Group g = ProActiveGroup.getGroup(result);

	for (int i=0 ; i < params.length ; i++)
	    g.add(ProActive.newActive(className, params[i], nodeList[i % nodeList.length]));
	
	return result;
    }


	/**
	 * Turns the target object (a typed group) into an ActiveObject (an active typed group) attached to a default
	 * node in the local JVM.
	 * @param <code>ogroup</code> the typed group to turn active. 
	 * @return a reference on the active object produced.
     * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
     * @throws ClassNotFoundException if the Class corresponding to <code>className</code> can't be found.
     * @throws ClassNotReifiableException if the Class corresponding to <code>className</code> can't be reify.
     * @throws NodeException if the node was null and that the DefaultNode cannot be created.
	 */
	public static Object turnActiveGroup(Object ogroup)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {
		return ProActive.turnActive(ogroup, ProActiveGroup.getType(ogroup), (Node) null, null, null);
	}


	/**
	 * Turns the target object (a typed group) into an ActiveObject (an active typed group) attached to a specified node.
	 * @param <code>ogroup</code> the typed group to turn active.
	 * @param <code>node</code> the node where to create the active object on. If <code>null</code>,
	 * the active object is created localy on a default node
	 * @return a reference (possibly remote) on the active object produced.
	 * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
	 * @throws ClassNotFoundException if the Class corresponding to <code>className</code> can't be found.
	 * @throws ClassNotReifiableException if the Class corresponding to <code>className</code> can't be reify.
	 * @throws NodeException if the specified node can not be reached.
	 */
	public static Object turnActiveGroup(Object ogroup, Node node)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {
		return ProActive.turnActive(ogroup, ProActiveGroup.getType(ogroup), node, null, null);
	}


	/**
	 * Turns the target object (a typed group) into an ActiveObject (an active typed group) attached to a specified node.
	 * @param <code>ogroup</code> the typed group to turn active.
	 * @param <code>nodeName</code> the name of the node where to create the active object on.
	 * @return a reference (possibly remote) on the active object produced.
	 * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
	 * @throws ClassNotFoundException if the Class corresponding to <code>className</code> can't be found.
	 * @throws ClassNotReifiableException if the Class corresponding to <code>className</code> can't be reify.
	 * @throws NodeException if the specified node can not be reached.
	 */
	public static Object turnActiveGroup(Object ogroup, String nodeName)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {
		return ProActive.turnActive(ogroup, ProActiveGroup.getType(ogroup), NodeFactory.getNode(nodeName), null, null);
	}


   /**
	* Creates an object representing a group (a typed group) and creates members with params cycling on nodeList.
    * Threads are used to build the group's members. This methods returns when all members were created.
	* @param <code>className</code> the name of the (upper) class of the group's member.
	* @param <code>params</code> the array that contain the parameters used to build the group's member.
	* @param <code>nodeList</code> the nodes where the members are created.
	* @return a typed group with its members.
    * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
    * @throws ClassNotFoundException if the Class corresponding to <code>className</code> can't be found.
    * @throws ClassNotReifiableException if the Class corresponding to <code>className</code> can't be reify.
    * @throws NodeException if the node was null and that the DefaultNode cannot be created
	*/
    public static Object newGroupBuildWithMultithreading(String className, Object[][] params, String[] nodeList)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {

	Object result = newGroup(className);
	Group g = ProActiveGroup.getGroup(result);

	for (int i = 0 ; i < params.length ; i++)
	    ((org.objectweb.proactive.core.group.ProxyForGroup)g).createThreadCreation(className, params[i], nodeList[i % nodeList.length]);

	((org.objectweb.proactive.core.group.ProxyForGroup)g).waitForAllCallsDone();

	return result;
    }
    
    
    /**
     * Waits for all the futures are arrived.
     * @param <code>o</code> a typed group.
     */
    public static void waitAll(Object o) {
		if (MOP.isReifiedObject (o)) {
	    	org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    	// If the object represents a group, we use the proxyForGroup's method
 	    	if (theProxy != null)
			((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).waitAll();
 	    	// Else the "standard waitFor" method has been used in the findProxyForGroup method
		}
    }
    
    
    
    /**
     * Waits for (at least) one future is arrived.
     * @param <code>o</code> a typed group.
     */
    public static void waitOne(Object o) {
		if (MOP.isReifiedObject (o)) {
	    	org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    	// If the object represents a group, we use the proxyForGroup's method
 	    	if (theProxy != null)
			((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).waitOne();
 	    	// Else the "standard waitFor" method has been used in the findProxyForGroup method
		}
    }


    
	/**
	 * Waits n futures are arrived.
	 * @param <code>o</code> a typed group.
	 * @param <code>n</code> the number of awaited members. 
	 */
    public static void waitN(Object o, int n) {
		if (MOP.isReifiedObject (o)) {
		    org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    	// If the object represents a group, we use the proxyForGroup's method
 	    	if (theProxy != null)
			((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).waitN(n);
 	    	// Else the "standard waitFor" method has been used in the findProxyForGroup method
		}
    }
    
			

    /**
     * Tests if all the members of the object <code>o</code> representing a group are awaited or not.
     * Always returns <code>false</code> if <code>o</code> is not a reified object (future or group).
	 * @param <code>o</code> a typed group.
	 * @return <code>true</code> if all the members of <code>o</code> are awaited.
     */
    public static boolean allAwaited (Object o) {
		// If the object is not reified, it cannot be a future (or a group of future)
		if (!(MOP.isReifiedObject (o)))
		    return false;
		else {
	    	org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    	// If the object represents a group, we use the proxyForGroup's method
 	    	if (theProxy != null)
			return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).allAwaited();
 	    	// Else the "standard waitFor" method has been used in the findProxyForGroup method so the future is arrived
	    	else
			return false;
		}
   	}
	    

    /**
     * Tests if all the member of the object <code>o</code> representing a group are arrived or not.
     * Always returns <code>true</code> if <code>o</code> is not a reified object (future or group).
	 * @param <code>o</code> a typed group.
	 * @return <code>true</code> if all the members of <code>o</code> are arrived.
     */
    public static boolean allArrived (Object o) {
    	// If the object is not reified, it cannot be a future (or a group of future)
		if (!(MOP.isReifiedObject (o)))
		    return true;
		else {
	    	org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    	// If the object represents a group, we use the proxyForGroup's method
 	    	if (theProxy != null)
				return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).allArrived();
 	    	// Else the "standard waitFor" method has been used in the findProxyForGroup method so the future is arrived
	    	else
				return true;
		}
    }


    /**
     * Waits one future is arrived and get it.
	 * @param <code>o</code> a typed group.
	 * @return a member of <code>o</code>.
     */
    public static Object waitAndGetOne (Object o) {
		if (MOP.isReifiedObject (o)) {
	    	org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    	// If the object represents a group, we use the proxyForGroup's method
	    	if (theProxy != null)
				return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).waitAndGetOne();
	    	// Else the "standard waitFor" method has been used in the findProxyForGroup method so the future is arrived, just return it
	    	else
				return o;
		}
		// if o is not a reified object just return it
		else
	    	return o;
    }


    /**
     * Wait the N-th future in the list is arrived.
	 * @param <code>o</code> a typed group.
     */
    public static void waitTheNth (Object o, int n) {
		if (MOP.isReifiedObject (o)) {
		    org.objectweb.proactive.core.mop.Proxy theProxy  = findProxyForGroup(o);
		    // If the object represents a group, we use the proxyForGroup's method
 	    	if (theProxy != null)
				((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).waitTheNth(n);
 	    	// Else the "standard waitFor" method has been used in the findProxyForGroup method
		}
    }


    /**
     * Wait the N-th future is arrived and get it.
	 * @param <code>o</code> a typed group.
	 * @param <code>n</code> the rank of the awaited member.
	 * @return the <code>n</code>-th member of th typed group <code>o</code>.
     */
    public static Object waitAndGetTheNth (Object o, int n) {
		if (MOP.isReifiedObject (o)) {
			org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    	// If the object represents a group, we use the proxyForGroup's method
	    	if (theProxy != null)
				return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).waitAndGetTheNth(n);
	    	// Else the "standard waitFor" method has been used in the findProxyForGroup method so the future is arrived, just return it
	    	else
				return o;
		}
		// if o is not a reified object just return it
		else
	    	return o;
    }

	/**
	 * Waits that at least one member is arrived and returns its index.
	 * @param <code>o</code> a typed group.
	 * @return the index of a non-awaited member of the Group, -1 if <code>o</code> is not a reified object.
	 */
	public int waitOneAndGetIndex(Object o) {
		if (MOP.isReifiedObject (o)) {
			org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
			// If the object represents a group, we use the proxyForGroup's method
			if (theProxy != null)
				return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).waitOneAndGetIndex();
			// Else return 0
			else
				return 0;
	}
	// if o is not a reified object, return -1
	else
		return -1;
	}

		
    /**
     * Returns the number of members of the object representing a Group.
     * Throws an IllegalArgumentException if <code>o</code> doesn't represent a Group.
	 * @param <code>o</code> a typed group.
	 * @return the number of member of the typed group <code>o</code>.
     */
    public static int size (Object o) {
    	org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
		if (theProxy == null)
		    throw new java.lang.IllegalArgumentException("Parameter doesn't represent a group");
		else
			return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).size();
    }
    

    /**
     * Returns the member at the specified index of the object representing a Group.
     * Returns <code>null</code> if <code>obj</code> doesn't represent a Group.
     * @param <code>o</code> a typed group.
	 * @param <code>n</code> the rank of the wanted member.
	 * @return the member of the typed group at the rank <code>n</code>
     */
    public static Object get (Object o, int n) {
    	org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
		if (theProxy == null)
		    return null;
		else 
	    	return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).get(n);
    }


    /**
     * Checks if the object <code>o</code> is an object representing a Group (future or not).
     * @param <code>o</code> the Object to check.
     * @return <code>true</code> if <code>o</code> is a typed group.  
     */  
    public static boolean isGroup (Object o) {	
	    return (findProxyForGroup(o) != null);
    }
    
    /**
     * Allows the typed group to dispatch parameters
     * @param <code>ogroup</code> the typed group who will change his semantic of communication.
     */
    public static void setScatterGroup(Object ogroup) {
		Proxy proxytmp = findProxyForGroup(ogroup);
		if (proxytmp != null)
			((ProxyForGroup)proxytmp).setDispatchingOn();
    }
    
    /**
     * Allows the typed group to broadcast parameters
     * @param <code>ogroup</code> the typed group who will change his semantic of communication.
     */
    public static void unsetScatterGroup(Object ogroup) {
 		Proxy proxytmp = findProxyForGroup(ogroup);
		if (proxytmp != null)
			((ProxyForGroup)proxytmp).setDispatchingOff();
   }

   /**
	* Allows the typed group to make an unique serialization of parameters when a broadcast call occurs.
	* @param <code>ogroup</code> the typed group who will change his semantic of communication.
	*/
   public static void setUniqueSerialization(Object ogroup) {
	   Proxy proxytmp = findProxyForGroup(ogroup);
	   if (proxytmp != null)
		   ((ProxyForGroup)proxytmp).setUniqueSerializationOn();
   }
    
   /**
	* Removes the ability of a typed group to make an unique serialization
	* @param <code>ogroup</code> the typed group who will change his semantic of communication.
	*/
   public static void unsetUniqueSerialization(Object ogroup) {
	   Proxy proxytmp = findProxyForGroup(ogroup);
	   if (proxytmp != null)
		   ((ProxyForGroup)proxytmp).setUniqueSerializationOff();
  }

   
    /**
     * Checks the semantic of communication of the typed group <code>ogroup</code>.
     * @param <code>ogroup</code> a typed group.
     * @return <code>true</code> if the "scatter option" is enabled for the typed group <code>ogroup</code>.
     */
    public static boolean isScatterGroupOn (Object ogroup) {
 		Proxy proxytmp = findProxyForGroup(ogroup);
		if (proxytmp != null)
			return ((ProxyForGroup)proxytmp).isDispatchingOn();
		else return false;
   }

    /**
     * Returns the ProxyForGroup of the typed group <code>ogroup</code>.
     * @param <code>ogroup</code> the typed group. 
     * @return the <code>ProxyForGroup</code> of the typed group <code>ogroup</code>.
     * <code>null</code> if <code>ogroup</code> does not represent a Group.
     */
    private static ProxyForGroup findProxyForGroup(Object ogroup) {
		if (!(MOP.isReifiedObject(ogroup)))
	    	return null;
		else {
	    	Proxy tmp = ((StubObject)ogroup).getProxy();

	    	// obj is an object representing a Group (and not a future)
	    	if (tmp instanceof org.objectweb.proactive.core.group.ProxyForGroup)
				return (org.objectweb.proactive.core.group.ProxyForGroup) tmp;
	    
	    	// obj is a future ... but may be a future-Group
	    	while (tmp instanceof org.objectweb.proactive.core.body.future.FutureProxy)
				// future of future ...
				if (MOP.isReifiedObject(((FutureProxy)tmp).getResult()))
		    		tmp = ((StubObject)((FutureProxy)tmp).getResult()).getProxy();
	        	// future of standard objet
				else
		    		return null;
	    
	    		// future-Group
	    	if (tmp instanceof org.objectweb.proactive.core.group.ProxyForGroup)
				return (org.objectweb.proactive.core.group.ProxyForGroup) tmp;
	    	// future of an active object
	    	else
				return null;
		}
	}


}
