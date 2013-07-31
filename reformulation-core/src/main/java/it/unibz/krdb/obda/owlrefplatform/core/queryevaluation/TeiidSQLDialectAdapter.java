/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import java.sql.Types;

public class TeiidSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlCast(String value, int type) {
		String strType = null;
		if (type == Types.VARCHAR) {
			strType = "VARCHAR";
		} else {
			throw new RuntimeException("Unsupported SQL type");
		}
		return "CAST(" + value + " AS " + strType + ")";
	}
}
