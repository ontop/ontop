package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Constant;

public interface BinaryAssertion extends Assertion {
	public Constant getValue1();

	public Constant getValue2();
}
