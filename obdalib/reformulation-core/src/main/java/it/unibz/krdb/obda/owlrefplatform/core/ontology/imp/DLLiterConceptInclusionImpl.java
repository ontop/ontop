package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PositiveInclusion;

public class DLLiterConceptInclusionImpl implements PositiveInclusion {

	private ConceptDescription including = null;//righthand side
	private ConceptDescription included = null;
	
	String string = null;
	
	public DLLiterConceptInclusionImpl(ConceptDescription concept1, ConceptDescription concept2){
		
		included = concept1;
		including = concept2;
		
		string = toString();
		
	}
	
	public ConceptDescription getIncluded(){
		return included;
	}
	
	public ConceptDescription getIncluding(){
		return including;
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof DLLiterConceptInclusionImpl))
			return false;
		DLLiterConceptInclusionImpl inc2 = (DLLiterConceptInclusionImpl)obj;
		if (!including.equals(inc2.including))
			return false;
		return (included.equals(inc2.included));
	}

	public String toString() {
		if (string != null)
			return string;
		
		StringBuffer bf = new StringBuffer();
		bf.append(included.toString());
		bf.append(" ISA ");
		bf.append(including.toString());
		return bf.toString();
	}
}
