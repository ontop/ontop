/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql;

public class ViewDefinition extends DataDefinition {


	private static final long serialVersionUID = 3312336193514797486L;

	private String statement;
	
	public ViewDefinition(String name) {
		super(name);
	}
	

	@Deprecated
	public void copy(String statement) {
		this.statement = statement;
	}
	
	public void setSQL(String statement) {
		this.statement = statement;
	}

	public String getStatement() {
		return statement;
	}

	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append(name);
		bf.append("[");
		boolean comma = false;
		for (Integer i : attributes.keySet()) {
			if (comma) {
				bf.append(",");
			}
			bf.append(attributes.get(i));
			comma = true;
		}
		bf.append("]");

		bf.append(String.format("   (%s)", statement));
		return bf.toString();
	}
}
