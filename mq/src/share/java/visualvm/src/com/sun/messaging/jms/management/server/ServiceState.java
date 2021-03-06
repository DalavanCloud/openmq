/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)ServiceState.java	1.5 07/02/07
 */ 

package com.sun.messaging.jms.management.server;

/**
 * Class containing information on service states.
 */
public class ServiceState {
    /** 
     * Unknown service state.
     */
    public static final int UNKNOWN = -1;

    /**
     * Service is up and running.
     */
    public static final int RUNNING = 0;

    /**
     * Service is paused.
     */
    public static final int PAUSED = 1;

    /**
     * Service is quiesced.
     */
    public static final int QUIESCED = 2;


    /*
     * Class cannot be instantiated
     */
    private ServiceState() {
    }
    
    /**
     * Returns a string representation of the specified service state.
     *
     * @param state Service state.
     * @return String representation of the specified service state.
     */
    public static String toString(int state)  {
        switch (state) {
            case RUNNING:
                return "RUNNING";

            case PAUSED:
                return "PAUSED";

            case QUIESCED:
                return "QUIESCED";

	    default:
                return "UNKNOWN";
        }
    }
}
