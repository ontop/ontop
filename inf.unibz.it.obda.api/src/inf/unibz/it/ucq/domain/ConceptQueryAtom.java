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


import inf.unibz.it.dl.domain.NamedConcept;

import java.util.ArrayList;

public class ConceptQueryAtom extends QueryAtom {

	private NamedConcept			concept	= null;
	private QueryTerm	term	= null;

	public ConceptQueryAtom(NamedConcept concept, QueryTerm term) {
		this.concept = concept;
		this.term = term;
	}

	@Override
	public String getName() {
		return concept.getName();
	}

	@Override
	public ArrayList<QueryTerm> getTerms() {
		ArrayList<QueryTerm> terms = new ArrayList<QueryTerm>();
		terms.add(term);
		return terms;
	}

	@Override
	public String toString() {
		return concept.toString() + "(" + term.toString() + ")";
	}

	@Override
	public ConceptQueryAtom clone() {
		return new ConceptQueryAtom(concept.clone(), term.clone());
	}
	
}
