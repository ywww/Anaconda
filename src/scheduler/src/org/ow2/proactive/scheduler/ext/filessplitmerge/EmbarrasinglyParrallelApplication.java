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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.filessplitmerge;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.ext.filessplitmerge.event.InternalSchedulerEventListener;
import org.ow2.proactive.scheduler.ext.filessplitmerge.exceptions.NotInitializedException;
import org.ow2.proactive.scheduler.ext.filessplitmerge.logging.LoggerManager;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.GeneralMenuCreator;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.MenuCreatorHoloder;
import org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.TextualUI;
import org.ow2.proactive.scheduler.ext.filessplitmerge.util.MySchedulerProxy;


/**
 * This class is to be used in order to create an Embarrassingly Parallel stand
 * alone Application Call initApplication(String schedulerURL, String userName,
 * String passwd, Class<? extends GeneralMenuCreator> menuCreatorClass, Class<?
 * extends JobPostTreatmentManager> postTreatmentManagerClass, Class<? extends
 * JobConfiguration> jobConfigClass) in order to initiate and start your
 * application
 * 
 * @author esalagea
 * 
 */

public class EmbarrasinglyParrallelApplication {

    private String ScheduelrURL;
    private String userName;
    private String password;

    private Class<? extends JobConfiguration> jobConfigurationClass;

    private static EmbarrasinglyParrallelApplication instance;

    private EmbarrasinglyParrallelApplication() {
    }

    /**
     * Singleton pattern
     * 
     * @return
     */
    public static EmbarrasinglyParrallelApplication instance() {
        if (instance == null) {
            instance = new EmbarrasinglyParrallelApplication();
        }
        return instance;
    }

    /**
     * 
     * @return UserName used by this application to connect to the scheduler
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 
     * @return the password used by this application to connect to the scheduler
     */
    public String getSchedulerPassword() {
        return password;
    }

    /**
     * 
     * @return the url used by this application to connect to the scheduler
     */
    public String getScheduelrURL() {
        return ScheduelrURL;
    }

    /**
     * This method initiate and start a Textual UI interface for your
     * application Before calling this method you must: - implement a Menu
     * Creator class by extending <{@link org.ow2.proactive.scheduler.ext.filessplitmerge.textualui.GeneralMenuCreator} -
     * implement a Job Post Treatment Manager class by extending <{@link JobPostTreatmentManager} -
     * implement a Job Configuration class by extending <{@link JobConfiguration}
     * 
     * This method will: Create and start a textual UI - this ui will receive
     * events from the (singleton) LoggerManager. The menu of this UI is the one
     * created by the Menu Creator that have been provided Creates a <{@link SchedulerProxyUserInterface}
     * active object and connects it to the Scheduler. This proxy will be used
     * in order to send demands to the Scheduler (i.e. add listeners to the
     * Scheduler, get job results, etc.) Creates an <{@link InternalSchedulerEventListener}
     * and adds it to the Scheduler (using the above proxy) - this object will
     * send the notifications received from the scheduler to its own listeners
     * and will also update its local view of the Scheduler Creates a <{@link JobPostTreatmentManager}
     * (of type that has been sent as argument) which will listen to the above
     * <code> InternalSchedulerEventListener </code> The TextualUI also listens
     * to the <{@link InternalSchedulerEventListener }
     * 
     * 
     * Note: if menucreatorClass argument is null, no textual UI will be created
     * To be used if you application is a Scheduler GUI plug-in or if it is
     * called from a script
     * 
     * @param schedulerURL
     * @param userName
     * @param passwd
     * @param menuCreatorClass
     * @param postTreatmentManagerClass
     * @param jobConfigClass
     */
    public void initApplication(String schedulerURL, String userName, String passwd,
            Class<? extends GeneralMenuCreator> menuCreatorClass,
            Class<? extends JobPostTreatmentManager> postTreatmentManagerClass,
            Class<? extends JobConfiguration> jobConfigClass) {

        this.userName = userName;
        this.password = passwd;
        this.ScheduelrURL = schedulerURL;
        this.jobConfigurationClass = jobConfigClass;

        boolean createUI = true;
        TextualUI ui = null;
        if (menuCreatorClass == null)
            createUI = false;

        /** --- CREATE TEXTUAL UI -----* */
        if (createUI) {
            ui = new TextualUI();
        }
        /** Init Loggermanager * */

        LoggerManager loggerManager = LoggerManager.getInstane();
        if (createUI) {
            loggerManager.addLogger(ui);
        }

        /** INIT MENU CREATOR HOLDER */
        if (createUI) {
            try {
                MenuCreatorHoloder.setMenuCreator(menuCreatorClass);
            } catch (InstantiationException e1) {
                LoggerManager.getInstane().error("Could not create textual UI. Application will exit", e1);
                e1.printStackTrace();
                System.exit(1);
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
                LoggerManager.getInstane().error("Could not create textual UI. Application will exit", e1);
                System.exit(1);
            }
        }

        /** INIT THE SCHEDULER PROXY */
        try {
            SchedulerProxyUserInterface proxy = MySchedulerProxy.getActiveInstance();
            proxy.init(schedulerURL, userName, passwd);
        } catch (ActiveObjectCreationException e1) {
            LoggerManager.getInstane().error("Could not create connection to the scheduler. ", e1);
            e1.printStackTrace();
            System.exit(1);
        } catch (NodeException e1) {
            LoggerManager.getInstane().error("Could not create connection to the scheduler. ", e1);
            e1.printStackTrace();
            System.exit(1);
        } catch (LoginException e) {
            LoggerManager.getInstane().error(
                    "Could not create connection to the scheduler. Invalid username and password.  ", e);
            e.printStackTrace();
            System.exit(1);
        } catch (SchedulerException e) {
            LoggerManager.getInstane().error(
                    "Could not create connection to the scheduler. Scheduler is not reachable. ", e);
            e.printStackTrace();
            System.exit(1);
        }

        /** INIT JOBPOST TREATMENT MANAGER */
        try {
            JobPostTreatmentManagerHolder.setPostTreatmentManager(postTreatmentManagerClass);
        } catch (InstantiationException e1) {
            LoggerManager.getInstane().error(
                    "Could not create  Post Treatment Manager. Application will exit", e1);
            System.exit(1);
        } catch (IllegalAccessException e1) {
            LoggerManager.getInstane().error(
                    "Could not create Post Treatment Manager. Application will exit", e1);
            System.exit(1);
        }

        JobPostTreatmentManager jptp = null;
        try {
            jptp = JobPostTreatmentManagerHolder.getPostTreatmentManager();
            jptp.init();
        } catch (NotInitializedException e1) {
            LoggerManager.getInstane().error(
                    "Could not create  Post Treatment Manager. Application will exit", e1);
            System.exit(1);
        }

        /** INIT LOCAL LISTENER * */
        /**
         * The Local listener will subscribe, as a listener, to the Scheduler
         * Proxy
         */
        InternalSchedulerEventListener[] refs;
        try {
            refs = InternalSchedulerEventListener.getActiveAndLocalReferences();

            // we have a local reference and an active reference on the same
            // object:
            InternalSchedulerEventListener internalSchedulerEnevtlistener_localRef = refs[0];
            InternalSchedulerEventListener internalSchedulerEnevtlistener_activeRef = refs[1];

            if (!internalSchedulerEnevtlistener_activeRef.isConnected().getBooleanValue()) {
                // System.out.println("localConnected:"+goldSchedulerEnevtlistener_localRef.isConnected().booleanValue());
                loggerManager.error("Could not connect listener to the scheduler. Application will exit. ");
                System.exit(0);
            }
            ;

            if (createUI)
                internalSchedulerEnevtlistener_localRef.addObserver(ui);
            internalSchedulerEnevtlistener_localRef.addObserver(jptp);

        } catch (ActiveObjectCreationException e) {
            loggerManager.error("Could not connect listener to the scheduler. Application will exit.", e);
            System.exit(0);

        } catch (NodeException e) {
            loggerManager.error("Could not connect listener to the scheduler. Application will exit.", e);
            System.exit(0);
        }

        /** Textual Ui -> start command listener -> show menu */
        if (createUI) {
            ui.startCommandListener();
        }

    }

    /**
     * 
     * @return The class that implements a <{@link JobConfiguration }
     */
    public Class<? extends JobConfiguration> getJobConfigurationClass() {
        return jobConfigurationClass;
    }

    /**
     * 
     * @param password
     *            used to connect to the Scheduler
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 
     * @param scheduelrURL
     *            url used to connect to the Scheduler
     */
    public void setScheduelrURL(String scheduelrURL) {
        ScheduelrURL = scheduelrURL;
    }

    /**
     * 
     * @param userName -
     *            user used to connect to the Scheduler
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 
     * @param jobConfigurationClass
     *            the class that implements a <{@link JobConfiguration }
     */
    public void setJobConfigurationClass(Class<? extends JobConfiguration> jobConfigurationClass) {
        this.jobConfigurationClass = jobConfigurationClass;
    }

}
