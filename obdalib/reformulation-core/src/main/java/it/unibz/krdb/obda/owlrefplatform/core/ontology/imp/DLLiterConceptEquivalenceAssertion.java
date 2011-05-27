package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;

import java.util.List;


public class DLLiterConceptEquivalenceAssertion implements Assertion{

List<ConceptDescription> equivalentClasses = null;
	
	public DLLiterConceptEquivalenceAssertion (List<ConceptDescription> equClasses){
		this.equivalentClasses = equClasses;
	}
	
	public List<ConceptDescription> getEquivalentClasses(){
		return equivalentClasses;
	}
}
