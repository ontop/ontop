/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql;

public class OracleConstraintTest extends AbstractConstraintTest {

	public OracleConstraintTest(String method) {
		super(method);
	}

	@Override
	protected String getConnectionPassword() {
		return "obdaps83";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:oracle:thin:@//10.7.20.91:1521/xe";
	}

	@Override
	protected String getConnectionUsername() {
		return "system";
	}

	@Override
	protected String getDriverName() {
		return "oracle.jdbc.OracleDriver";
	}
}
