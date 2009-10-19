//============================================================================
// Name        : ProActive Embarrassingly Parallel Framework 
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.logging;

public interface GenericLogger {

    public void warning(String msg);

    public void info(String msg);

    public void warning(String message, Exception e);

    public void error(String message);

    public void error(String message, Exception e);

}
