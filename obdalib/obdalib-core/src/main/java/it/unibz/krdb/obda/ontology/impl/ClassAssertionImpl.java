package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.UnaryAssertion;

import java.util.HashSet;
import java.util.Set;

public class ClassAssertionImpl implements ClassAssertion, UnaryAssertion {

	private static final long serialVersionUID = 5689712345023046811L;

	private ObjectConstant object = null;

	private Predicate concept = null;

	ClassAssertionImpl(Predicate concept, ObjectConstant object) {
		this.object = object;
		this.concept = concept;
	}

	@Override
	public ObjectConstant getObject() {
		return object;
	}

	@Override
	public Predicate getConcept() {
		return concept;
	}

	public String toString() {
		return concept.toString() + "(" + object.toString() + ")";
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		res.add(concept);
		return res;
	}

	@Override
	public int getArity() {
		return 1;
	}

	@Override
	public Constant getValue() {
		return getObject();
	}

	@Override
	public Predicate getPredicate() {
		return concept;
	}
}
