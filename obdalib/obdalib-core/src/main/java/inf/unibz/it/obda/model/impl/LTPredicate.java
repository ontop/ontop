package inf.unibz.it.obda.model.impl;

import inf.unibz.it.obda.model.impl.LTPredicate;
import inf.unibz.it.obda.model.impl.PredicateImp;

import java.net.URI;

import org.obda.query.domain.ComparisonOperatorPredicate;

public class LTPredicate extends ComparisonOperatorPredicate {

	private URI name = URI.create("http://www.obda.org/ucq/predicate/operator/comparison#LT");
	private int identifier = -3;
	
	public int getArity() {
		// TODO Auto-generated method stub
		return 0;
	}

	public URI getName() {
		// TODO Auto-generated method stub
		return name;
	}
	
	public boolean equals(Object obj){
		if(obj == null|| !(obj instanceof PredicateImp)){
			return false;
		}else{
			return this.hashCode() == obj.hashCode();
		}	
	}
	
	public int hashCode(){
		return identifier;
	}

	public LTPredicate copy() {
		
		return new LTPredicate();
	}
}
