package org.obda.owlrefplatform.core.ontology.imp;

import java.util.List;

import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.owlrefplatform.core.ontology.ConceptDescription;

public class DLLiterDisjointConceptAssertion implements Assertion{

	List<ConceptDescription> disjointClasses = null;
	
	public DLLiterDisjointConceptAssertion (List<ConceptDescription> disjointClasses){
		this.disjointClasses = disjointClasses;
	}
}
