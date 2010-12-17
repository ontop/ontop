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

import inf.unibz.it.obda.api.controller.APIController;

@Deprecated
public class VariableTerm extends QueryTerm {

	public VariableTerm(String name) {
		super(name);
	}

	@Override
	public String toString() {		
		return "$" + this.getVariableName();
	}

	@Override
	public VariableTerm clone() {
		return new VariableTerm(new String(this.getVariableName()));
	}
	
	

}
