package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;

import java.util.List;


public class DLLiterDisjointConceptAssertion implements Assertion{

	List<ConceptDescription> disjointClasses = null;
	
	public DLLiterDisjointConceptAssertion (List<ConceptDescription> disjointClasses){
		this.disjointClasses = disjointClasses;
	}
}
