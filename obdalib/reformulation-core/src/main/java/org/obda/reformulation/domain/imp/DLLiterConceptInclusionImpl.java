package org.obda.reformulation.domain.imp;

import org.obda.reformulation.domain.ConceptDescription;
import org.obda.reformulation.domain.PositiveInclusion;

public class DLLiterConceptInclusionImpl implements PositiveInclusion {

	private ConceptDescription including = null;//righthand side
	private ConceptDescription included = null;
	
	public DLLiterConceptInclusionImpl(ConceptDescription concept1, ConceptDescription concept2){
		
		included = concept1;
		including = concept2;
	}
	
	public ConceptDescription getIncluded(){
		return included;
	}
	
	public ConceptDescription getIncluding(){
		return including;
	}
}
