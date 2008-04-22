package functionalTests.resourcemanager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.util.MutableInteger;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMEvent;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMEventType;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMInitialState;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMMonitoring;


public class RMEventReceiver implements InitActive, RunActive, RMEventListener {

    private MutableInteger nbEventReceived = new MutableInteger(0);

    RMMonitoring monitor;

    // array List to store nodes urls of events thrown by RMMonitoring
    /** list of freed nodes urls */
    private ArrayList<String> freeNodes;

    /** list of busied nodes urls */
    private ArrayList<String> busyNodes;

    /** list of all fallen nodes urls */
    private ArrayList<String> downNodes;

    /** list of all nodes become 'to be released' */
    private ArrayList<String> toBeReleasedNodes;

    /** list of all nodes become removed */
    private ArrayList<String> removedNodes;

    private Vector<String> methodCalls;

    /**
     * ProActive Empty constructor
     */
    public RMEventReceiver() {
    }

    public RMEventReceiver(RMMonitoring m) {
        monitor = m;
        freeNodes = new ArrayList<String>();
        busyNodes = new ArrayList<String>();
        downNodes = new ArrayList<String>();
        toBeReleasedNodes = new ArrayList<String>();
        removedNodes = new ArrayList<String>();

        methodCalls = new Vector<String>();
        for (Method method : RMEventListener.class.getMethods()) {
            methodCalls.add(method.getName());
        }
    }

    public void cleanEventLists() {
        freeNodes.clear();
        busyNodes.clear();
        downNodes.clear();
        toBeReleasedNodes.clear();
        removedNodes.clear();
    }

    public void initActivity(Body body) {
        // TODO Auto-generated method stub
        RMInitialState initState = monitor.addRMEventListener((RMEventListener) PAActiveObject
                .getStubOnThis(), RMEventType.NODE_ADDED, RMEventType.NODESOURCE_CREATED,
                RMEventType.NODE_BUSY, RMEventType.NODE_DOWN, RMEventType.NODE_FREE,
                RMEventType.NODE_REMOVED, RMEventType.NODE_TO_RELEASE);
        PAActiveObject.setImmediateService("waitForNEvent");
    }

    public void runActivity(Body body) {
        Service s = new Service(body);
        while (body.isActive()) {
            Request r = s.blockingRemoveOldest();
            s.serve(r);
            if (methodCalls.contains(r.getMethodName())) {
                synchronized (this.nbEventReceived) {
                    this.nbEventReceived.add(1);
                    this.nbEventReceived.notify();
                }
            }
        }
    }

    public void waitForNEvent(int nbEvents) throws InterruptedException {
        synchronized (this.nbEventReceived) {
            while ((this.nbEventReceived.getValue() < nbEvents)) {
                this.nbEventReceived.wait();
            }
            this.nbEventReceived.add(-nbEvents);
        }
    }

    public ArrayList<String> cleanNgetNodesBusyEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) busyNodes.clone();
        busyNodes.clear();
        return toReturn;
    }

    public ArrayList<String> cleanNgetNodesFreeEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) this.freeNodes.clone();
        this.freeNodes.clear();
        return toReturn;
    }

    public ArrayList<String> cleanNgetNodesToReleaseEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) this.toBeReleasedNodes.clone();
        this.toBeReleasedNodes.clear();
        return toReturn;

    }

    public ArrayList<String> cleanNgetNodesdownEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) this.downNodes.clone();
        this.downNodes.clear();
        return toReturn;

    }

    public ArrayList<String> cleanNgetNodesremovedEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) this.removedNodes.clone();
        this.removedNodes.clear();
        return toReturn;

    }

    // methods override RMEventListener
    public void nodeBusyEvent(RMNodeEvent n) {
        busyNodes.add(n.getNodeUrl());
    }

    public void nodeDownEvent(RMNodeEvent n) {
        downNodes.add(n.getNodeUrl());
    }

    public void nodeToReleaseEvent(RMNodeEvent n) {
        toBeReleasedNodes.add(n.getNodeUrl());
    }

    public void nodeRemovedEvent(RMNodeEvent n) {
        removedNodes.add(n.getNodeUrl());
    }

    public void nodeFreeEvent(RMNodeEvent n) {
        freeNodes.add(n.getNodeUrl());
    }

    public void nodeAddedEvent(RMNodeEvent n) {
    }

    public void nodeSourceAddedEvent(RMNodeSourceEvent ns) {
    }

    public void nodeSourceRemovedEvent(RMNodeSourceEvent ns) {
        // nothing for this test
    }

    public void rmKilledEvent(RMEvent evt) {
        // nothing for this test
    }

    public void rmShutDownEvent(RMEvent evt) {
        // nothing for this test
    }

    public void rmShuttingDownEvent(RMEvent evt) {
    }

    public void rmStartedEvent(RMEvent evt) {
    }
}
