package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.SubDescriptionAxiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;

public class SubPropertyAxiomImpl implements SubDescriptionAxiom {

	private Property	including	= null;
	private Property	included	= null;

	String					string		= null;

	SubPropertyAxiomImpl(Property included, Property including) {

		this.including = including;
		this.included = included;

		this.string = toString();

	}

	public Property getSub() {
		return included;
	}

	public Property getSuper() {
		return including;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object obj) {

		if (!(obj instanceof SubPropertyAxiomImpl))
			return false;
		SubPropertyAxiomImpl inc2 = (SubPropertyAxiomImpl) obj;
		if (!including.equals(inc2.including))
			return false;
		return (included.equals(inc2.included));
	}

	public String toString() {
		if (string != null)
			return string;
		StringBuffer bf = new StringBuffer();
		bf.append(included.toString());
		bf.append(" ISAR ");
		bf.append(including.toString());
		return bf.toString();
	}

}
