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
 * @(#)Monitor.hpp	1.4 06/26/07
 */ 

#ifndef MONITOR_HPP
#define MONITOR_HPP

#include <nspr.h>

/** This class is used to ensure synchronous access to shared data. It
    delegates to the NSPR monitor, PRMonitor*/
class Monitor {
private:
  /** The NSPR monitor */
  PRMonitor *   monitor;

  /** The nested depth of the monitor.  This is only used for detecting
   *  errors.  For example, if the monitor is deleted when depth != 0.*/
  PRInt32 depth;

public:
  Monitor();
  virtual ~Monitor();

  /** Enter a critical section */
  void enter();

  /** Leave a critical section */
  void exit();

  /** Wait until the monitor is signalled */
  void wait();

  /** Wait until the monitor is signalled or timeout expires */
  void wait(const PRIntervalTime timeout);

  /** Signal one waiter */
  void notifyOne();
  
  /** Signal all waiters */
  void notifyAll();

//
// Avoid all implicit shallow copies.  Without these, the compiler
// will automatically define implementations for us.
//
private:
  //
  // These are not supported and are not implemented
  //
  Monitor(const Monitor& monitor);
  Monitor& operator=(const Monitor& monitor);
};


#endif // MONITOR_HPP
