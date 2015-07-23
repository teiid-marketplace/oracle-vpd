/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.jboss.teiid.oracle;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleDriver;

/**
 * This MUST be used with pass-through authentication in JBoss EAP, where
 * current user's credentials are used as VPD user and what configured under
 * "global-user", "global-password" in -ds.xml used as regular user/password
 * for obtaining the connection.
 * 
 * If there is way to define the OSUSER on existing connection, this class would not be
 * needed. 
 */
public class OracleVPDDriver implements java.sql.Driver {
    private oracle.jdbc.OracleDriver delegate = new OracleDriver();
    static Logger logger = Logger.getLogger("org.jboss.teiid.oracle"); //$NON-NLS-1$
    
    public boolean acceptsURL(String arg0) {
        return delegate.acceptsURL(arg0);
    }

    public Connection connect(String arg0, Properties arg1) throws SQLException {
        String vpdUser = arg1.getProperty("user");
        String vpdPassword = arg1.getProperty("password");

        String user = arg1.getProperty("global-user");
        String password = arg1.getProperty("global-password");
                       
        if (user != null) {
            arg1.remove("user");
            arg1.remove("password");
            
            arg1.setProperty("user", user);
            arg1.setProperty("password", password);
            arg1.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_VSESSION_OSUSER, vpdUser);
        }
        return delegate.connect(arg0, arg1);
    }

    public Connection defaultConnection() throws SQLException {
        return delegate.defaultConnection();
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        //return delegate.getParentLogger();
        return logger;
    }

    public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1)
            throws SQLException {
        return delegate.getPropertyInfo(arg0, arg1);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean jdbcCompliant() {
        return delegate.jdbcCompliant();
    }

    public String processSqlEscapes(String arg0) throws SQLException {
        return delegate.processSqlEscapes(arg0);
    }

    public String toString() {
        return delegate.toString();
    }
}
