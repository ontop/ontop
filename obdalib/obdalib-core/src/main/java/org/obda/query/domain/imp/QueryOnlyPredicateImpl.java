package org.obda.query.domain.imp;

import java.net.URI;

import org.obda.query.domain.Predicate;
import org.obda.query.domain.QueryOnlyPredicate;

public class QueryOnlyPredicateImpl implements QueryOnlyPredicate{

	private int arity = -1;
	private URI name = null;
	private int identifier = -1;

	protected QueryOnlyPredicateImpl (URI name, int identifier, int arity){
		this.name = name;
		this.identifier = identifier;
		this.arity = arity;

		if(identifier < -59999 || identifier > -50000){
			throw new IllegalArgumentException("The identifier for QueryOnlyPredicate must be in the intervall [-59999,-50000]");
		}
	}

	public int getArity() {
		return arity;
	}

	public URI getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj){

		if(obj == null|| !(obj instanceof PredicateImp)){
			return false;
		}else{
			return this.hashCode() == obj.hashCode();
		}
	}

	@Override
	public int hashCode(){
		return identifier;
	}

	public Predicate copy() {
		return new QueryOnlyPredicateImpl(name, identifier, arity);
	}

}
