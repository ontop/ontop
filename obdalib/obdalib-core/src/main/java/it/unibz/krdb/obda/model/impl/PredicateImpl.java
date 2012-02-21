package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Predicate;

import java.net.URI;

public class PredicateImpl implements Predicate {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7096056207721170465L;
	private int arity = -1;
	private URI name = null;
	private int identifier = -1;
	private COL_TYPE[] types = null;
	

	protected PredicateImpl(URI name, int arity, COL_TYPE[] types) {
		this.name = name;
		this.identifier = name.toString().hashCode();
		this.arity = arity;
		this.types = types;
		
	}

	// public void setName(URI name) {
	// this.name = name;
	// }

	@Override
	public int getArity() {
		return arity;
	}

	@Override
	public URI getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || !(obj instanceof PredicateImpl))
			return false;

		PredicateImpl pred2 = (PredicateImpl) obj;
		if (pred2.arity != arity)
			return false;

		return this.identifier == pred2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public Predicate clone() {
		return this;
//		PredicateImpl clone = new PredicateImpl(this.name, this.arity, types);
//		clone.identifier = identifier;		
//		return clone;
	}

	@Override
	public String toString() {
		return name.toString();
	}

	@Override
	public COL_TYPE getType(int column) {
		if (types != null)
			return types[column];
		return null;
	}


}
