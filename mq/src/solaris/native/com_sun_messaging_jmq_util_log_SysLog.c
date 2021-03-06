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

#ifndef lint
static  char sccsid[] = "@(#)com_sun_messaging_jmq_util_log_SysLog.c	1.6 07/02/07 Copyr 2002 Sun Microsystems, Inc.";
#endif

/*
 * @(#)com_sun_messaging_jmq_util_log_SysLog.c	1.6 07/02/07
 */ 

#include <syslog.h>
#include <langinfo.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <iconv.h>
#include <stdlib.h>

#include "com_sun_messaging_jmq_util_log_SysLog.h"

#define MY_BUFSIZ (8 * 1024)

/*
 * For state-dependent encodings, changes the state of the conversion
 * descriptor to initial shift state.  Also, outputs the byte sequence
 * to change the state to initial state.
 * This code is assuming the iconv call for initializing the state
 * won't fail due to lack of space in the output buffer.
 *
 * NOTE! We're not using this now since we call iconv_open and close
 * for each message
 */
#define INIT_SHIFT_STATE(cd, fptr, ileft, tptr, oleft) \
    { \
        fptr = NULL; \
        ileft = 0; \
        tptr = to; \
        oleft = MY_BUFSIZ; \
        (void) iconv(cd, &fptr, &ileft, &tptr, &oleft); \
        (void) fwrite(to, 1, MY_BUFSIZ - oleft, stdout); \
    }


/*
 * Convert from one codeset to another. This function assumes the 
 * output buffer is large enough to perform the conversion in.
 * If it is not, or there is any other problem this function returns
 * a non-zero value.
 *
 * Since most log messages are of reasonable size this shouldn't be
 * a problem.
 */
int
iconv_main(const char *to_code,   char *to,   size_t to_len,
           const char *from_code, const char *from, size_t from_len)
{
    iconv_t cd;
    char    *tptr;
    const char  *fptr;
    size_t  ileft, oleft, num, ret;


    cd = iconv_open((const char *)to_code, (const char *)from_code);
    if (cd == (iconv_t)-1) {
        /*
         * iconv_open failed
         */
        /*
        (void) fprintf(stderr,
            "iconv_open(%s, %s) failed\n", to_code, from_code);
        */
        return (1);
    }

    /* (void) fprintf(stderr, "iconv_open(%s, %s)\n", to_code, from_code); */

    fptr = from;
    tptr = to;
    ileft = from_len;
    oleft = to_len;

#if !defined(AIX)
    ret = iconv(cd, &fptr, &ileft, &tptr, &oleft);
#else
    ret = iconv(cd, (char **)&fptr, &ileft, &tptr, &oleft);
#endif
    if (ret == (size_t)-1) {
        /* failed */
        (void) iconv_close(cd);
        return errno;
    }

    /*
     * Initializes the conversion descriptor and outputs
     * the sequence to change the state to initial state.
     */
    /* INIT_SHIFT_STATE(cd, fptr, ileft, tptr, oleft); */

    (void) iconv_close(cd);
    return (0);
}

JNIEXPORT jint JNICALL
Java_com_sun_messaging_jmq_util_log_SysLog_mySetLogMask(JNIEnv *env, jclass thisClass, jint mask) {
    return (jint)setlogmask((int)mask);
}


JNIEXPORT void JNICALL
Java_com_sun_messaging_jmq_util_log_SysLog_syslog(JNIEnv *env, jclass thisClass, jint priority, jstring msg) {

    const char  *from;
    char        *from_code = "UTF-8";
    char        *to_code;
    char        to[MY_BUFSIZ];
    const char  *utf_string;
    int         rcode = 0;
    int         utf_len = 0;
    jboolean    isCopy;

    memset(to, '\0', MY_BUFSIZ);

    /*
     * The string from Java is encoded using unicode. We must convert it
     * to the platform's native encoding, otherwise it will appear in syslog
     * as escaped UTF-8 ascii.
     */

    /* Get platform encoding */
    to_code = (char *)nl_langinfo(CODESET);

    /* Get UTF-8 encoded string and length */
    utf_string = (*env)->GetStringUTFChars(env, msg, &isCopy);
    utf_len    = (*env)->GetStringUTFLength(env, msg);

    from = utf_string;

    /* Convert from UTF-8 to native code set */
    rcode = iconv_main(to_code,   to,   MY_BUFSIZ,
                       from_code, from, utf_len);

    if (rcode == 0) {
        /* conversion suceeded */
        syslog((int)priority, to);
    } else {
        /*
         * conversion failed. Log original un-converted string. For most
         * western languages this will at least be readable.
         */
        /* (void) fprintf(stderr, "iconv failed: %d\n", rcode); */
        /* perror("iconv failed: "); */
        syslog((int)priority, utf_string);
    }

    if (isCopy == JNI_TRUE) {
        (*env)->ReleaseStringUTFChars(env, msg, utf_string);
    }
}

JNIEXPORT void JNICALL
Java_com_sun_messaging_jmq_util_log_SysLog_openlog(JNIEnv *env, jclass thisClass, jstring ident,
    jint option, jint facility) {

    const char *utf_string;
    jboolean isCopy;

    utf_string = (*env)->GetStringUTFChars(env, ident, &isCopy);

    /* XXX Need to check and map facilities */
    openlog(utf_string, (int)option, (int)facility);

    /* We don't release string because it is used by syslog */
}

JNIEXPORT void JNICALL
Java_com_sun_messaging_jmq_util_log_SysLog_closelog(JNIEnv *env, jclass thisClass) {
    closelog();
}

