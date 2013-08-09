/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.messaging.jmq.jmsserver.service.imq.websocket;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.DataFrame;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.util.ServiceType;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.GoodbyeReason;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQService;
import com.sun.messaging.jmq.jmsserver.service.imq.websocket.stomp.STOMPWebSocket;
import com.sun.messaging.jmq.jmsserver.service.imq.websocket.json.JSONWebSocket;

/**
 * @author amyk
 */
public class MQWebSocketServiceApp extends WebSocketApplication {

    private static final String SUBPROTOCOL_V12STOMP = "v12.stomp";

    protected static final String JMS = "mqjms";
    protected static final String STOMP = "mqstomp";
    protected static final String JSONSTOMP = "mqjsonstomp";
    private static final String JMS_REQUEST_PATH = "/"+JMS; 
    private static final String STOMP_REQUEST_PATH = "/"+STOMP; 
    private static final String JSON_REQUEST_PATH = "/"+JSONSTOMP; 

    private Logger logger = Globals.getLogger();
    private BrokerResources br = Globals.getBrokerResources();

    private Map<MQWebSocket, WebSocketMQIPConnection> wsMQConnMap = 
        Collections.synchronizedMap(new HashMap<MQWebSocket, WebSocketMQIPConnection>());

    private WebSocketIPService service = null;


    public MQWebSocketServiceApp(WebSocketIPService wss) {
        this.service = wss;
    }

    public IMQService getMQService() {
        return service;
    }

    protected static boolean isSupportedSubService(String subserv) {
        if (subserv.equals(JMS) || 
            subserv.equals(STOMP) || 
            subserv.equals(JSONSTOMP)) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> getSupportedProtocols(List<String> subProtocol) {
        if (subProtocol.contains(SUBPROTOCOL_V12STOMP)) {
            List<String> l = new ArrayList<String>();
            l.add(SUBPROTOCOL_V12STOMP);
            return l;
        } else {
            return super.getSupportedProtocols(subProtocol);
        }
    }

    private boolean isJMSRequest(HttpRequestPacket request) {
        return ("/"+service.getName()+JMS_REQUEST_PATH).equals(request.getRequestURI());
    }

    private boolean isSTOMPRequest(HttpRequestPacket request) {
        return ("/"+service.getName()+STOMP_REQUEST_PATH).equals(request.getRequestURI());
    }

    private boolean isJSONRequest(HttpRequestPacket request) {
        return ("/"+service.getName()+JSON_REQUEST_PATH).equals(request.getRequestURI());
    }

    @Override
    public WebSocket createSocket(ProtocolHandler handler,
                                  HttpRequestPacket request,
                                  WebSocketListener... listeners) {
        if (isJMSRequest(request)) {
            return new JMSWebSocket(this, handler, request, listeners);
        }
        if (isJSONRequest(request)) {
            return new JSONWebSocket(this, handler, request, listeners);
        }
        if (isSTOMPRequest(request)) {
            return new STOMPWebSocket(this, handler, request, listeners);
        } 
        throw new UnsupportedOperationException(
            "MQWebSocketServiceApp.createSocket("+request.getRequestURI()+")");
    }

    @Override
    public void onConnect(WebSocket wsocket) {
        super.onConnect(wsocket);
        try {
            if (wsocket instanceof JMSWebSocket) {
                WebSocketMQIPConnection conn = service.createConnection((MQWebSocket)wsocket);
                wsMQConnMap.put((MQWebSocket)wsocket, conn);
                Globals.getConnectionManager().addConnection(conn);
                if (MQWebSocket.getDEBUG()) {
                    logger.log(Logger.INFO,
                    "MQWebSocketServiceApplication.onConnect("+wsocket+"): "+conn);
                }
            } else {
                if (MQWebSocket.getDEBUG()) {
                    logger.log(Logger.INFO,
                    "MQWebSocketServiceApplication.onConnect("+wsocket+")");
                }
            }
        } catch (Exception e) {
            logger.logStack(logger.ERROR, e.getMessage(), e);
        }
    }

    @Override
    public boolean isApplicationRequest(HttpRequestPacket request) {
        if (MQWebSocket.getDEBUG()) {
            logger.log(Logger.INFO, "isApplicationRequest("+request+") called:"+request.getRequestURI());
        }
        boolean jms = false, stomp = false, json = false;
        if ((jms = isJMSRequest(request)) || 
            (stomp = isSTOMPRequest(request)) || 
            (json = isJSONRequest(request))) {
            if (MQWebSocket.getDEBUG()) {
                logger.log(Logger.INFO, "isApplicationRequest("+request+
                "): found match[jms="+jms+", stomp="+stomp+", json="+json+"]");
            }
            if ((jms && !service.isSubServiceEnabled(JMS)) ||
                (stomp && !service.isSubServiceEnabled(STOMP)) ||
                (json && !service.isSubServiceEnabled(JSONSTOMP))) {
                String emsg = br.getKString(br.X_SERVICETYPE_NO_SUPPORT,
                              request.getRequestURI(), service.getName());
                logger.log(logger.WARNING, emsg);
                return false;
            }
            if (isSTOMPRequest(request) || isJSONRequest(request)) {
                if (service.getServiceType() != ServiceType.NORMAL) { 
                    String emsg = br.getKString(br.X_SERVICETYPE_NO_SUPPORT,
                        ServiceType.getServiceTypeString(service.getServiceType()),
                        service.getName()+"["+request.getRequestURI()+"]");
                    logger.log(logger.WARNING, emsg);
                    return false;
                }
            }
            return true;
        }
        String emsg = br.getKString(br.W_UNKNOWN_REQUEST_ON_SERVICE,
                          request.getRequestURI(), service.getName());
        logger.log(logger.WARNING, emsg);
        return false;
    }

    @Override
    public void onMessage(WebSocket wsocket, String text) {
        if (MQWebSocket.getDEBUG()) {
            logger.log(Logger.INFO, 
            Thread.currentThread()+"MQWebSocketServiceApp.onMessage("+wsocket+", text="+text+")");
        }
        try {
            if (!(wsocket instanceof JSONWebSocket) &&
                !(wsocket instanceof STOMPWebSocket)) {
                logger.log(Logger.ERROR, br.getKString(
                    br.W_UNKNOWN_REQUEST_ON_SERVICE,
                    "WebSocket.onMessage("+wsocket+", String)", service.getName()));
                wsocket.close(WebSocket.INVALID_DATA);
                return;
            }
            ((MQWebSocket)wsocket).processData(text);
        } catch (Exception e) {
            logger.logStack(Logger.ERROR, e.getMessage(), e);
        }
    }

    @Override
    public void onMessage(WebSocket wsocket, byte[] data) {
        if (MQWebSocket.getDEBUG()) {
            logger.log(Logger.INFO, 
            Thread.currentThread()+"MQWebSocketServiceApp.onMessage("+wsocket+", bytes.len="+data.length+")");
        }
 
        try {
            if (wsocket instanceof JSONWebSocket) { 
                logger.log(Logger.ERROR, br.getKString(
                    br.W_UNKNOWN_REQUEST_ON_SERVICE,
                    "WebSocket.onMessage("+wsocket+", byte[])", service.getName()));
                wsocket.close(WebSocket.INVALID_DATA);
                return;
            }
            ((MQWebSocket)wsocket).processData(data);
        } catch (Exception e) {
            logger.logStack(Logger.ERROR, e.getMessage(), e);
        }
    }

    @Override
    public void onClose(WebSocket wsocket, DataFrame frame) {
        super.onClose(wsocket, frame);
	WebSocketMQIPConnection conn = wsMQConnMap.get(wsocket);
	if (MQWebSocket.getDEBUG()) {
            logger.log(Logger.INFO,
            "MQWebSocketServiceApp.onClose(): "+conn+"["+wsocket+"]");
	}
        if (conn != null) {
            if (conn.getConnectionState() < WebSocketMQIPConnection.STATE_CLOSED) {
		try {
                    conn.destroyConnection(true, GoodbyeReason.CLIENT_CLOSED,
                                           br.getKString(br.M_CONNECTION_CLOSE));
                } catch (Exception e) {
                    if (MQWebSocket.getDEBUG()) {
                        logger.log(Logger.WARNING, e.getMessage(), e);
                    }
                }
            }
            wsMQConnMap.remove(wsocket); 
	}
        logger.log(Logger.INFO, br.getKString(br.I_ClOSED_WEBSOCKET, 
            "@"+wsocket.hashCode()+"["+wsocket+"]["+frame+"]"));
    }

    public void onPing(WebSocket wsocket, byte[] bytes) {
	if (MQWebSocket.getDEBUG()) {
            logger.log(Logger.INFO, Thread.currentThread()+
                "MQWebSocketServiceApp.onPing("+wsocket+", bytes.len="+bytes.length);
        }
    }

    public void onPong(WebSocket wsocket, byte[] bytes) {
	if (MQWebSocket.getDEBUG()) {
            logger.log(Logger.INFO, Thread.currentThread()+
                "MQWebSocketServiceApp.onPong("+wsocket+", bytes.len="+bytes.length);
        }
    }

    public void onFragment(WebSocket wsocket, String fragment, boolean last) {
	if (MQWebSocket.getDEBUG()) {
            logger.log(Logger.INFO, Thread.currentThread()+
                "MQWebSocketServiceApp.onFragment("+wsocket+", text="+fragment+", last="+last);
        }
        try {
            ((MQWebSocket)wsocket).processData(fragment);
        } catch (Exception e) {
            logger.logStack(Logger.ERROR, e.getMessage(), e);
        }
    }

    public void onFragment(WebSocket wsocket, byte[] fragment, boolean last) {
        if (MQWebSocket.getDEBUG()) {
            logger.log(Logger.INFO, Thread.currentThread()+
                "MQWebSocketServiceApp.onFragment("+wsocket+", bytes.len="+fragment.length+", last="+last);
        }
        try {
            ((MQWebSocket)wsocket).processData(fragment);
        } catch (Exception e) {
            logger.logStack(Logger.ERROR, e.getMessage(), e);
        }
    }

    protected WebSocketMQIPConnection getMQIPConnection(MQWebSocket wsocket) {
        return wsMQConnMap.get(wsocket);
    }
}
