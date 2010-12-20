package org.obda.query.domain.imp;

import java.net.URI;

import org.obda.query.domain.Predicate;

public class PredicateImp implements Predicate{

	private int arity = -1;
	private URI name = null;
	private int identfier = -1;

	protected PredicateImp (URI name, int identifier, int arity){
		this.name = name;
		this.identfier = identifier;
		this.arity = arity;
	}

	public int getArity() {
		return arity;
	}

	public URI getName() {
		return name;
	}

	public void setName(URI name) {
		this.name = name;
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
		return identfier;
	}

	public Predicate copy() {
		return new PredicateImp(this.name, this.identfier, this.arity);
	}
}
