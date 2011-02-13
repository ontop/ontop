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

import java.net.URI;
import java.util.ArrayList;

@Deprecated
public class FunctionTerm extends QueryTerm {

	private ArrayList<QueryTerm> function_terms = null;
	private URI uri = null;
	
	public FunctionTerm(URI uri, ArrayList<QueryTerm> terms) {
		super(uri.getFragment());
		this.uri = uri;
		this.function_terms = terms;
	}
	
	public ArrayList<QueryTerm> getParameters() {
		return function_terms;
	}

	@Override
	public FunctionTerm clone() {
		ArrayList<QueryTerm> clonedTerms = new ArrayList<QueryTerm>();
		for (int i = 0; i < function_terms.size(); i++) {
			clonedTerms.add(function_terms.get(i).clone());
		}
		return new FunctionTerm(this.getURI(), clonedTerms);
	}

	public URI getURI(){
		return uri;
	}
	
	@Override
	public String getVariableName(){
		
		return uri.getFragment();
	}
}
