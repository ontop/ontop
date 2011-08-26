package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.SubDescriptionAxiom;

public class SubClassAxiomImpl implements SubDescriptionAxiom {

	private ClassDescription including = null;//righthand side
	private ClassDescription included = null;
	
	String string = null;
	
	public SubClassAxiomImpl(ClassDescription concept1, ClassDescription concept2){
		if (concept1 == null || concept2 == null)
			throw new RuntimeException("Recieved null in concept inclusion");
		included = concept1;
		including = concept2;
		
		string = toString();
		
	}
	
	public ClassDescription getSub(){
		return included;
	}
	
	public ClassDescription getSuper(){
		return including;
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof SubClassAxiomImpl))
			return false;
		SubClassAxiomImpl inc2 = (SubClassAxiomImpl)obj;
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
