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


import inf.unibz.it.dl.domain.DataProperty;
import inf.unibz.it.dl.domain.NamedProperty;
import inf.unibz.it.dl.domain.ObjectProperty;

import java.util.ArrayList;

public class BinaryQueryAtom extends QueryAtom {
	NamedProperty relation = null;
	QueryTerm term1 = null;
	QueryTerm term2 = null;

	public BinaryQueryAtom(NamedProperty relation, QueryTerm term1, QueryTerm term2) {
		this.relation = relation;
		this.term1 = term1;
		this.term2 = term2;
	}
	
	@Override
	public String getName() {
		return relation.getName();
	}

	@Override
	public ArrayList<QueryTerm> getTerms() {
		ArrayList<QueryTerm> terms = new ArrayList<QueryTerm>();
		terms.add(term1);
		terms.add(term2);
		return terms;
	}

	@Override
	public String toString() {
		return relation.toString() + "(" + term1.toString()+ "," + term2.toString() + ")";
	}

	@Override
	public BinaryQueryAtom clone() {
		if (relation instanceof DataProperty)
			return new BinaryQueryAtom(((DataProperty)relation).clone(), term1.clone(), term2.clone());
		if (relation instanceof ObjectProperty)
			return new BinaryQueryAtom(((ObjectProperty)relation).clone(), term1.clone(), term2.clone());
		return null;
	}
	

}
