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
package org.ow2.proactive.scheduler.gui.actions;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.wizards.flatJobWizard.FlatFileJobWizard;


/**
 * @author The ProActive Team
 */
public class SubmitFlatFileJobAction extends SchedulerGUIAction {

    public SubmitFlatFileJobAction() {
        this.setText("Submit a file containing commands");
        this.setToolTipText("Submit a file containing commands");
        this
                .setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                        Internal.IMG_FILEOBJ));
        this.setEnabled(false);
    }

    @Override
    public void run() {
        // Instantiates and initializes the wizard
        FlatFileJobWizard wizard = new FlatFileJobWizard();
        //wizard.init(null, null);

        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog(getParent().getShell(), wizard);
        dialog.create();
        dialog.open();
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus schedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        if (connected && (schedulerStatus != SchedulerStatus.KILLED) &&
            (schedulerStatus != SchedulerStatus.SHUTTING_DOWN) &&
            (schedulerStatus != SchedulerStatus.STOPPED))
            setEnabled(true);
        else
            setEnabled(false);
    }
}
