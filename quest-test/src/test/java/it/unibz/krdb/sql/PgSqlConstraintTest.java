/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql;

public class PgSqlConstraintTest extends AbstractConstraintTest {

	public PgSqlConstraintTest(String method) {
		super(method);
	}

	@Override
	protected String getConnectionPassword() {
		return "postgres";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:postgresql://10.7.20.39/dbconstraints";
	}

	@Override
	protected String getConnectionUsername() {
		return "postgres";
	}

	@Override
	protected String getDriverName() {
		return "org.postgresql.Driver";
	}
}
