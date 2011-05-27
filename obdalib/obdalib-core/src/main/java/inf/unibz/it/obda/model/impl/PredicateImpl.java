package inf.unibz.it.obda.model.impl;

import inf.unibz.it.obda.model.Predicate;

import java.net.URI;

public class PredicateImpl implements Predicate{

	private int arity = -1;
	private URI name = null;
	private int identifier = -1;

	protected PredicateImpl (URI name, int arity){
		this.name = name;
		this.identifier = name.toString().hashCode();
		this.arity = arity;
	}

	public void setName(URI name) {
		this.name = name;
	}

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

		if (obj == null|| !(obj instanceof PredicateImpl))
			return false;

		PredicateImpl pred2 = (PredicateImpl) obj;
		if (pred2.arity != arity)
			return false;

		return this.identifier == pred2.identifier;
	}

	@Override
	public int hashCode(){
		return identifier;
	}

	@Override
	public Predicate copy() {
		return new PredicateImpl(this.name, this.arity);
	}

	@Override
	public String toString() {
		return name.toString();
	}
}
