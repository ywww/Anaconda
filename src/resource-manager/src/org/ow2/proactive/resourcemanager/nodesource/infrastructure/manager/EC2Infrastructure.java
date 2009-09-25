/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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

package org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.ec2.EC2Deployer;
import org.ow2.proactive.resourcemanager.nodesource.policy.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.utils.FileToBytesConverter;

import com.xerox.amazonws.ec2.ReservationDescription.Instance;


/**
 *
 * Amazon Elastic Compute Cloud Infrastructure
 * <p>
 * Has a maximum node value, which means at least X nodes can be
 * deployed at the same time, to control the instances' costs
 * <p>
 * This Infrastructure supports acquiring nodes one by one,
 * all nodes at the same time, and releasing them one by one or every one of
 * them at the same time when necessary.
 * Node acquisition suffers a 2mn delay at best, and node release is immediate
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 *
 */
public class EC2Infrastructure extends InfrastructureManager {

    @Configurable(fileBrowser = true)
    protected File configurationFile;
    @Configurable
    protected String rmUrl;
    @Configurable
    protected String rmLogin;
    @Configurable(password = true)
    protected String rmPass;
    @Configurable
    protected String nodeHttpPort = "80";

    /**
     * Deployment data
     */
    protected EC2Deployer ec2d;

    /** used to schedule the instance timeout checker */
    private transient Timer timer = null;
    /** requested instances waiting for the timeout checker's confirmation */
    private List<Instance> instances = new ArrayList<Instance>();

    /** delay after which a requested EC2 instance is considered lost in ms */
    private static final long timeoutDelay = 60000 * 45; // 45mn
    /** timeout checker frequency in ms */
    private static final long timeoutCheckerFreq = 60000 * 10; // 10mn

    /**
     * Image descriptor to use will try the first available if null
     */
    String imgd;

    /** logger */
    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);

    /**
     * Default constructor
     */
    public EC2Infrastructure() {

    }

    /**
     * {@inheritDoc}
     */
    public void acquireAllNodes() {
        if (timer == null) {
            timer = new Timer(true);
            timer.schedule(new NodeChecker(), timeoutCheckerFreq, timeoutCheckerFreq);
        }

        synchronized (this.ec2d) {
            if (ec2d.canGetMoreNodes()) {
                try {
                    int num = ec2d.getMaxInstances() - ec2d.getCurrentInstances();
                    ec2d.setNsName(nodeSource.getName());
                    this.instances.addAll(ec2d.runInstances(imgd));
                    logger.info("Successfully acquired " + num + " EC2 instance" + ((num > 1) ? "s" : ""));
                } catch (Exception e) {
                    logger.error("Unable to acquire all EC2 instances", e);
                }
            } else {
                logger.info("Maximum simultaneous EC2 reservations already attained");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void acquireNode() {
        if (timer == null) {
            timer = new Timer(true);
            timer.schedule(new NodeChecker(), timeoutCheckerFreq, timeoutCheckerFreq);
        }

        synchronized (this.ec2d) {
            if (ec2d.canGetMoreNodes()) {
                try {
                    this.ec2d.setNsName(nodeSource.getName());
                    this.instances.addAll(ec2d.runInstances(1, 1, imgd));
                    logger.info("Successfully acquired an EC2 instance");
                    return;
                } catch (Exception e) {
                    logger.error("Unable to acquire EC2 instance", e);
                }
            } else {
                logger.info("Maximum simultaneous EC2 reservations already attained");
            }
        }
    }

    /**
     * Configures the Infrastructure
     *
     * @param parameters
     *            parameters[0]: Configuration file as byte array
     *            parameters[1]: Fully qualified URL of the Resource Manager (proto://IP:port)
     *            parameters[2]: RM login
     *            parameters[3]: RM passw
     *            parameters[4]: HTTP node port
     * @throws RMException
     *             when the configuration could not be set
     */
    public void addNodesAcquisitionInfo(Object... parameters) throws RMException {

        /** parameters look fine */
        if (parameters != null && parameters.length == 5) {
            try {
                //                File ff = new File(parameters[0].toString());
                byte[] configFile = (byte[]) parameters[0];
                File ff = File.createTempFile("configFile", "props");
                FileToBytesConverter.convertByteArrayToFile(configFile, ff);
                readConf(ff.getAbsolutePath());
                ff.delete();
            } catch (Exception e) {
                readConf(PAResourceManagerProperties.RM_EC2_PATH_PROPERTY_NAME.getValueAsString());
                logger.debug("Expected File as 1st parameter for EC2Infrastructure: " + e.getMessage());
            }
            String rmu = parameters[1].toString();
            String rml = parameters[2].toString();
            String rmp = parameters[3].toString();
            String nodep = parameters[4].toString();

            try {
                int pp = Integer.parseInt(nodep);
                if (pp < 10 || pp > 65535) {
                    throw new Exception("Out of range");
                }
            } catch (Exception e) {
                throw new RMException("Invalid value for parameter Node Port", e);
            }

            this.ec2d.setUserData(rmu, rml, rmp, nodep);

        }
        /**
         * missing or absent parameters, aborting
         */
        else {
            throw new RMException("Invalid parameters for EC2Infrastructure creation");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeNode(Node node, boolean forever) throws RMException {

        synchronized (this.ec2d) {

            InetAddress addr = node.getVMInformation().getInetAddress();
            if (!isThereNodesInSameJVM(node)) {
                logger.info("No node left, closing instance on URL :" + addr.toString());

                if (this.ec2d.terminateInstanceByAddr(addr)) {
                    logger.info("Instance closed: " + addr.toString());
                    return;
                } else {
                    logger.error("Could not close instance: " + addr.toString());
                }

            } else {
                try {
                    node.getProActiveRuntime().killNode(node.getNodeInformation().getName());
                } catch (ProActiveException e) {
                    logger.error("Could not kill node: " + node.getNodeInformation().getName() + " on " +
                        addr.toString());
                }
            }
        }
    }

    /**
     * Check if there are any other nodes handled by the NodeSource in the same JVM of the node
     * passed in parameter.
     *
     * @param node
     *            Node to check if there any other node of the NodeSource in the same JVM
     * @return true there is another node in the node's JVM handled by the nodeSource, false
     *         otherwise.
     */
    public boolean isThereNodesInSameJVM(Node node) {
        VMID nodeID = node.getVMInformation().getVMID();
        String nodeToTestUrl = node.getNodeInformation().getURL();
        Collection<Node> nodesList = nodeSource.getAliveNodes();
        for (Node n : nodesList) {
            if (!n.getNodeInformation().getURL().equals(nodeToTestUrl) &&
                n.getVMInformation().getVMID().equals(nodeID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Node source description
     */
    public String getDescription() {
        return "Handles nodes from the Amazon Elastic Compute Cloud Service.";
    }

    /**
     * Attemps to read a configuration file, fills deployment description fields on success
     *
     * @param path
     *            path to the configuration file
     * @throws RMException
     *             incorrect file or missing properties in file
     */
    private void readConf(String path) throws RMException {
        File fp = new File(path);

        if (!fp.isAbsolute()) {
            fp = new File(PAResourceManagerProperties.RM_HOME.getValueAsString() + File.separator + path);
            if (!fp.exists()) {
                throw new RMException("Could not find configuration file: " + path);
            }
        }

        Properties props = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(fp);
            props.load(in);
        } catch (Exception e) {
            throw new RMException("Error while reading EC2 properties: " + e.getMessage());
        }

        /** check all mandatory fields are present */
        if (!props.containsKey("AWS_AKEY"))
            throw new RMException("Missing property AWS_AKEY in: " + path);
        if (!props.containsKey("AWS_SKEY"))
            throw new RMException("Missing property AWS_SKEY in: " + path);
        if (!props.containsKey("AWS_USER"))
            throw new RMException("Missing property AWS_USER in: " + path);
        if (!props.containsKey("AMI"))
            throw new RMException("Missing property AMI in: " + path);
        if (!props.containsKey("INSTANCE_TYPE"))
            throw new RMException("Missing property INSTANCE_TYPE: " + path);
        if (!props.containsKey("MAX_INST"))
            throw new RMException("Missing property MAX_INST in: " + path);

        this.ec2d = new EC2Deployer(props.getProperty("AWS_AKEY"), props.getProperty("AWS_SKEY"), props
                .getProperty("AWS_USER"));

        this.imgd = props.getProperty("AMI");
        int maxInsts = 1;
        try {
            maxInsts = Integer.parseInt(props.getProperty("MAX_INST"));
        } catch (Exception e) {
            maxInsts = 1;
        }

        this.ec2d.setNumInstances(1, maxInsts);
        this.ec2d.setInstanceType(props.getProperty("INSTANCE_TYPE"));

        try {
            in.close();
        } catch (IOException e) {
            throw new RMException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "EC2 Infrastructure";
    }

    /**
     * {@inheritDoc}
     */
    public void registerAcquiredNode(Node node) throws RMException {
        synchronized (this.ec2d) {
            InetAddress nodeAddr = node.getVMInformation().getInetAddress();
            List<Instance> ic = new ArrayList<Instance>();
            ic.addAll(instances);

            for (Instance inst : ic) {
                try {
                    String ec2Host = this.ec2d.getInstanceHostname(inst.getInstanceId());
                    if (ec2Host.equals("")) {
                        continue;
                    }
                    InetAddress ec2Addr = InetAddress.getByName(ec2Host);
                    if (nodeAddr.equals(ec2Addr)) {
                        instances.remove(inst);
                        logger.info("Found requested EC2 instance: " + nodeAddr.toString());
                        return;
                    }
                } catch (Exception e) {
                    logger.error("Could not check node " + node.getNodeInformation().getURL() + ": " +
                        e.getMessage());
                }
            }
            logger.warn("New node " + nodeAddr.toString() + " was not a requested EC2 instance.");
        }
    }

    /**
     * Cleanup deployed instances,
     * so that instances that did not register to the nodesource be removed as well
     * 
     * @see org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.InfrastructureManager#shutDown()
     */
    @Override
    public void shutDown() {
        timer.cancel();

        synchronized (this.ec2d) {

            int ret = this.ec2d.terminateAll();
            if (ret > 0) {
                logger.info("Terminated " + ret + " EC2 nodes.");
            }

        }
    }

    /**
     * Checks requested EC2 instances successfully register to the nodesource
     * before a given timeout period. Terminate the EC2 instance and redeploy 
     * one to keep the nodesource's state consistent if a timeout is detected
     */
    private class NodeChecker extends TimerTask {

        @Override
        public void run() {
            int redeploy = 0;

            synchronized (ec2d) {
                List<Instance> ic = new ArrayList<Instance>();
                ic.addAll(instances);
                for (Instance inst : ic) {
                    long t1 = inst.getLaunchTime().getTimeInMillis();
                    long t2 = System.currentTimeMillis();
                    // this time difference is wildly inaccurate: t1 depends on amazon's clock, t2 the RM's
                    if (t2 - t1 > timeoutDelay) {
                        logger.info("Instance " + inst.getInstanceId() + " timed out, terminating.");
                        ec2d.terminateInstance(inst);
                        instances.remove(inst);
                        redeploy++;

                    }
                }
            }
            while (redeploy > 0) {
                acquireNode();
                redeploy--;
            }
        }
    }
}