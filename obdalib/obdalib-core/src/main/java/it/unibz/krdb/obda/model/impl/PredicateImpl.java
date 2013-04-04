package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.AlgebraOperatorPredicate;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.NumericalOperationPredicate;
import it.unibz.krdb.obda.model.Predicate;

import com.hp.hpl.jena.iri.IRI;

public class PredicateImpl implements Predicate {

	private static final long serialVersionUID = -7096056207721170465L;

	private int arity = -1;
	private IRI name = null;
	private int identifier = -1;
	private COL_TYPE[] types = null;

	protected PredicateImpl(IRI name2, int arity, COL_TYPE[] types) {
		this.name = name2;
		this.identifier = name2.toString().hashCode();
		this.arity = arity;
		this.types = types;
	}

	@Override
	public int getArity() {
		return arity;
	}

	@Override
	public IRI getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PredicateImpl)) {
			return false;
		}
		PredicateImpl pred2 = (PredicateImpl) obj;
		return this.identifier == pred2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public Predicate clone() {
		return this;
	}

	@Override
	public String toString() {
		return name.toString();
	}

	@Override
	public COL_TYPE getType(int column) {
		if (types != null) {
			return types[column];
		}
		return null;
	}

	@Override
	public boolean isClass() {
		if (arity == 1 && types[0] == COL_TYPE.OBJECT) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isObjectProperty() {
		if (arity == 2 && types[0] == COL_TYPE.OBJECT && types[1] == COL_TYPE.OBJECT) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isDataProperty() {
		if (arity == 2 && types[0] == COL_TYPE.OBJECT && types[1] == COL_TYPE.LITERAL) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isDataPredicate() {
		return (!(isBooleanPredicate() || isAlgebraPredicate() || isArithmeticPredicate() || isDataTypePredicate()));
	}

	@Override
	public boolean isBooleanPredicate() {
		return this instanceof BooleanOperationPredicate;
	}
	
	@Override
	public boolean isArithmeticPredicate() {
		return this instanceof NumericalOperationPredicate;
	}

	@Override
	public boolean isAlgebraPredicate() {
		return this instanceof AlgebraOperatorPredicate;
	}

	@Override
	public boolean isDataTypePredicate() {
		return this instanceof DataTypePredicate;
	}
}
