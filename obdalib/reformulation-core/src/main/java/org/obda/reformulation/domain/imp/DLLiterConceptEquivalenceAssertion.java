package org.obda.reformulation.domain.imp;

import java.util.List;

import org.obda.reformulation.domain.Assertion;
import org.obda.reformulation.domain.ConceptDescription;

public class DLLiterConceptEquivalenceAssertion implements Assertion{

List<ConceptDescription> equivalentClasses = null;
	
	public DLLiterConceptEquivalenceAssertion (List<ConceptDescription> equClasses){
		this.equivalentClasses = equClasses;
	}
	
	public List<ConceptDescription> getEquivalentClasses(){
		return equivalentClasses;
	}
}
