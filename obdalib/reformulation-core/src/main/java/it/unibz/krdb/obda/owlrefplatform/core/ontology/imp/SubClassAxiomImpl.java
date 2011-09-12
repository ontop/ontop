package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;

public class SubClassAxiomImpl extends AbstractSubDescriptionAxiom {


	SubClassAxiomImpl(ClassDescription concept1, ClassDescription concept2) {
		super(concept1, concept2);
	}

	public ClassDescription getSub() {
		return (ClassDescription) included;
	}

	public ClassDescription getSuper() {
		return (ClassDescription) including;
	}


	public boolean equals(Object obj) {
		if (!(obj instanceof SubClassAxiomImpl))
			return false;
		SubClassAxiomImpl inc2 = (SubClassAxiomImpl) obj;
		if (!including.equals(inc2.including))
			return false;
		return (included.equals(inc2.included));
	}



}
