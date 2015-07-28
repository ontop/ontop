package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;

public class CQContainmentCheckSyntactic implements CQContainmentCheck {

	/**
	 * Check if query cq1 is contained in cq2, syntactically. That is, if the
	 * head of cq1 and cq2 are equal according to toString().equals and each
	 * atom in cq2 is also in the body of cq1 (also by means of toString().equals().
	 */ 
	 
	@Override	
	public boolean isContainedIn(CQIE cq1, CQIE cq2) {
		if (!cq2.getHead().equals(cq1.getHead())) 
			return false;

		for (Function atom : cq2.getBody())
			if (!cq1.getBody().contains(atom))
				return false;
		
		return true;
	}

	
	@Override
	public Substitution computeHomomorphsim(CQIE q1, CQIE q2) {
		throw new RuntimeException("Unimplemented method");
	}

}
