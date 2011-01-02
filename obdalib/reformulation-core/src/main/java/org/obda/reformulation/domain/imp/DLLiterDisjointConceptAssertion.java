package org.obda.reformulation.domain.imp;

import java.util.List;

import org.obda.reformulation.domain.Assertion;
import org.obda.reformulation.domain.ConceptDescription;

public class DLLiterDisjointConceptAssertion implements Assertion{

	List<ConceptDescription> disjointClasses = null;
	
	public DLLiterDisjointConceptAssertion (List<ConceptDescription> disjointClasses){
		this.disjointClasses = disjointClasses;
	}
}
