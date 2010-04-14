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

import java.util.ArrayList;

public class FunctionTerm extends QueryTerm {

	private ArrayList<QueryTerm> function_terms = null;
	
	public FunctionTerm(String name, ArrayList<QueryTerm> terms) {
		super(name);
		this.function_terms = terms;
	}
	
	public ArrayList<QueryTerm> getParameters() {
		return function_terms;
	}

	@Override
	public String toString() {
		StringBuffer string = new StringBuffer();
		string.append(this.getName());
		string.append('(');
		string.append(function_terms.get(0).toString());
		for (int i = 1; i < function_terms.size(); i++) {
			string.append("," + function_terms.get(i).toString());
		}
		string.append(')');
		return string.toString();
	}

	@Override
	public FunctionTerm clone() {
		ArrayList<QueryTerm> clonedTerms = new ArrayList<QueryTerm>();
		for (int i = 0; i < function_terms.size(); i++) {
			clonedTerms.add(function_terms.get(i).clone());
		}
		return new FunctionTerm(new String(this.getName()), clonedTerms);
	}

	
	
	
}
