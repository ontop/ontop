package org.obda.query.domain.imp;

import java.net.URI;

import org.obda.query.domain.Predicate;

public class PredicateImp implements Predicate{

	private int arity = -1;
	private URI name = null;
	private int identfier = -1;

	protected PredicateImp (URI name, int arity){
		this.name = name;
		this.identfier = name.toString().hashCode();
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

		if (obj == null|| !(obj instanceof PredicateImp))
			return false;

		PredicateImp pred2 = (PredicateImp) obj;
		if (pred2.arity != arity)
			return false;

		return this.identfier == pred2.identfier;
	}

	@Override
	public int hashCode(){
		return identfier;
	}

	@Override
	public Predicate copy() {
		return new PredicateImp(this.name, this.arity);
	}

	@Override
	public String toString() {
		return getName().toString();
	}
}
