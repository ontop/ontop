/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql;

public class MssqlMetadataInfoTest extends AbstractDbMetadataInfoTest {

	@Override
	protected String getConnectionPassword() {
		return "obdaps83";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:sqlserver://10.7.20.91;databaseName=datatypes";
	}

	@Override
	protected String getConnectionUsername() {
		return "mssql";
	}

	@Override
	protected String getDriverName() {
		return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}
}
