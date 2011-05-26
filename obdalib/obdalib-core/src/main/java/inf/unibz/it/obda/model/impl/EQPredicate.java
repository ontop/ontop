package inf.unibz.it.obda.model.impl;

import inf.unibz.it.obda.model.impl.EQPredicate;
import inf.unibz.it.obda.model.impl.PredicateImp;

import java.net.URI;

import org.obda.query.domain.ComparisonOperatorPredicate;

public class EQPredicate extends ComparisonOperatorPredicate {

	private URI name = URI.create("http://www.obda.org/ucq/predicate/operator/comparison#EQ");
	private int identifier = -6;
	
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

	public EQPredicate copy() {
		
		return new EQPredicate();
	}

}
