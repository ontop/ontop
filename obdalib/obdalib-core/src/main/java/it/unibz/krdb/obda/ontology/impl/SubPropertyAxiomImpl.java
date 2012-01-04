package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.ontology.Property;

public class SubPropertyAxiomImpl extends AbstractSubDescriptionAxiom {

	SubPropertyAxiomImpl(Property included, Property including) {

		super(included, including);
	}

	public Property getSub() {
		return (Property) included;
	}

	public Property getSuper() {
		return (Property) including;
	}

	public boolean equals(Object obj) {

		if (!(obj instanceof SubPropertyAxiomImpl))
			return false;
		SubPropertyAxiomImpl inc2 = (SubPropertyAxiomImpl) obj;
		if (!including.equals(inc2.including))
			return false;
		return (included.equals(inc2.included));
	}

}
