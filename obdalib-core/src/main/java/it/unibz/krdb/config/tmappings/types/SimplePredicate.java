package it.unibz.krdb.config.tmappings.types;

import it.unibz.krdb.obda.model.Predicate;

public class SimplePredicate {
	private final String name;
	private final int arity;
	
	public SimplePredicate(String name, int arity){
		this.name = name;
		this.arity = arity;
	}
	
	public SimplePredicate(Predicate p){
		this.name = p.getName();
		this.arity = p.getArity();
	}
	
	public boolean isEqualTo(Predicate to){
		String toName = to.getName();
		int toArity = to.getArity();
		
		return toName == this.name && toArity == this.arity;
	}
	
	@Override 
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof SimplePredicate) {
			SimplePredicate that = (SimplePredicate) other;
			result = (this.arity == that.arity && this.name == that.name);
		}
		return result;
	}
	
	@Override 
	public int hashCode() {
		return this.name.hashCode() + arity;
	}
}
