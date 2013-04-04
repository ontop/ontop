package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.ontology.ClassDescription;

public class SubClassAxiomImpl extends AbstractSubDescriptionAxiom {

	private static final long serialVersionUID = -7590338987239580423L;

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
		if (!(obj instanceof SubClassAxiomImpl)) {
			return false;
		}
		SubClassAxiomImpl inc2 = (SubClassAxiomImpl) obj;
		if (!including.equals(inc2.including)) {
			return false;
		}
		return (included.equals(inc2.included));
	}
}
