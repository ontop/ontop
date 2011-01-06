package org.obda.owlrefplatform.core.ontology.imp;

import java.util.List;

import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.owlrefplatform.core.ontology.ConceptDescription;

public class DLLiterConceptEquivalenceAssertion implements Assertion{

List<ConceptDescription> equivalentClasses = null;
	
	public DLLiterConceptEquivalenceAssertion (List<ConceptDescription> equClasses){
		this.equivalentClasses = equClasses;
	}
	
	public List<ConceptDescription> getEquivalentClasses(){
		return equivalentClasses;
	}
}
