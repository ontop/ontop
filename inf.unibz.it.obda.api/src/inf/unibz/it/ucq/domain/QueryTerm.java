/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.ucq.domain;

public abstract class QueryTerm implements Cloneable {
	private String name;
	
	public QueryTerm(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public abstract String toString();
	public abstract QueryTerm clone();
	
	@Override
	public boolean equals(Object term) {
		if (!(term instanceof QueryTerm))
			return false;
		return name.equals(((QueryTerm)term).getName());
	}
}
