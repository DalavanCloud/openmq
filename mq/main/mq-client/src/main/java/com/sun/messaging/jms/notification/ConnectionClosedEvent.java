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
 * @(#)ConnectionClosedEvent.java	1.4 07/02/07
 */ 

package com.sun.messaging.jms.notification;

import com.sun.messaging.jms.Connection;
import com.sun.messaging.jmq.jmsclient.resources.ClientResources;

import javax.jms.JMSException;

/**
 * MQ Connection closed Event.  This event is generated by MQ and delivered
 * to an application (if the connection event listener is set) when a
 * connection is closed by MQ.
 */
public class ConnectionClosedEvent extends ConnectionEvent {

    // if there is any exception that caused the connection to be closed,
    //it is set to this event.
    private JMSException exception = null;

    /**
     * Connection closed event code - admin requested shutdown
     */
    public static final String CONNECTION_CLOSED_SHUTDOWN =
                               ClientResources.E_CONNECTION_CLOSED_SHUTDOWN;

    /**
     * Connection closed event code - admin requested restart
     */
    public static final  String CONNECTION_CLOSED_RESTART =
                               ClientResources.E_CONNECTION_CLOSED_RESTART;

    /**
     * Connection closed event code - server error, e.g. out of memory.
     */
    public static final String CONNECTION_CLOSED_ERROR =
                               ClientResources.E_CONNECTION_CLOSED_ERROR;

     /**
     * Connection closed event code - admin killed connection.
     */
    public static final String CONNECTION_CLOSED_KILL =
                               ClientResources.E_CONNECTION_CLOSED_KILL;

     /**
     * Connection closed event code - broker crash.
     */
    public static final String CONNECTION_CLOSED_BROKER_DOWN =
                               ClientResources.E_CONNECTION_CLOSED_BROKER_DOWN;

    /**
     * Connection closed event code - broker is not responsive.
     */
     public static final String CONNECTION_CLOSED_NON_RESPONSIVE =
                             ClientResources.E_CONNECTION_CLOSED_NON_RESPONSIVE;


    /**
     * The above event codes are for events originated from the broker.
     * Broker notifies MQ client runtime that the connection is closed.
     *
     * This event code is to represent that the MQ client runtime
     * detects the connection to the broker is broken.  This could be a network
     * problem or broker crashed.
     */
    public static final String CONNECTION_CLOSED_LOST_CONNECTION =
                               ClientResources.E_CONNECTION_CLOSED_LOST_CONNECTION;

    /**
     * Construct a connection closed event.
     *
     * @param conn the connection that the event is associated with.
     *             MQ may automatically reconnect to the same broker
     *             or a different broker depends on the client runtime
     *             configuration.
     * @param evCode the event code that represents this event object.
     * @param evMessage the event message that describes this event object.
     * @param jmse the JMSException that caused this event.
     */
    public ConnectionClosedEvent
        (Connection conn, String evCode, String evMessage, JMSException jmse) {

        super (conn, evCode, evMessage);

        this.exception = jmse;
    }

    /**
     * Get the JMSException that caused the connection to be closed.
     *
     * @return the JMSException that caused the connection to be closed.
     *         return null if no JMSException associated with this event.
     *         Such as connection closed caused by admin requested shutdown.
     */
    public JMSException getJMSException() {
        return exception;
    }

}
