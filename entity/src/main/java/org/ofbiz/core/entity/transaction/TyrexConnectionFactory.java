/*
 * $Id: TyrexConnectionFactory.java,v 1.1 2005/04/01 05:58:03 sfarquhar Exp $
 *
 *  Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ofbiz.core.entity.transaction;

import java.util.*;
import java.sql.*;

import com.atlassian.util.concurrent.CopyOnWriteMap;
import org.w3c.dom.Element;

import org.ofbiz.core.entity.*;
import org.ofbiz.core.util.*;

// For Tyrex 0.9.8.5
import tyrex.resource.jdbc.xa.*;

// For Tyrex 0.9.7.0
// import tyrex.jdbc.xa.*;

/**
 * Tyrex ConnectionFactory - central source for JDBC connections from Tyrex
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision: 1.1 $
 * @since      2.0
 */
public class TyrexConnectionFactory {

    protected static Map<String, EnabledDataSource> dsCache = CopyOnWriteMap.newHashMap();

    public static Connection getConnection(String helperName, Element inlineJdbcElement) throws SQLException, GenericEntityException {
        boolean usingTyrex = true;

        if (usingTyrex) {
            EnabledDataSource ds;

            // try once
            ds = (EnabledDataSource) dsCache.get(helperName);
            if (ds != null) {
                return TransactionUtil.enlistConnection(ds.getXAConnection());
            }

            synchronized (TyrexConnectionFactory.class) {
                // try again inside the synch just in case someone when through while we were waiting
                ds = (EnabledDataSource) dsCache.get(helperName);
                if (ds != null) {
                    return TransactionUtil.enlistConnection(ds.getXAConnection());
                }

                ds = new EnabledDataSource();
                ds.setDriverClassName(inlineJdbcElement.getAttribute("jdbc-driver"));
                ds.setDriverName(inlineJdbcElement.getAttribute("jdbc-uri"));
                ds.setUser(inlineJdbcElement.getAttribute("jdbc-username"));
                ds.setPassword(inlineJdbcElement.getAttribute("jdbc-password"));
                ds.setDescription(helperName);

                String transIso = inlineJdbcElement.getAttribute("isolation-level");

                if (transIso != null && transIso.length() > 0)
                    ds.setIsolationLevel(transIso);

                ds.setLogWriter(Debug.getPrintWriter());

                dsCache.put(helperName, ds);
                return TransactionUtil.enlistConnection(ds.getXAConnection());
            }
        }

        return null;
    }
}

