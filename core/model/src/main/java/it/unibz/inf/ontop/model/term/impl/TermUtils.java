package it.unibz.inf.ontop.model.term.impl;


import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Collection;

public class TermUtils {

	public static void addReferencedVariablesTo(Collection<Variable> vars, Function f) {
		for (Term t : f.getTerms()) {
			if (t instanceof Variable) 
				vars.add((Variable)t);
			else if (t instanceof Function)
				addReferencedVariablesTo(vars, (Function)t);
			// else (t instanceof BNode) || (t instanceof IRIConstant) || (t instanceof ValueConstant)
			// no-op
		}
	}
		
	public static void addReferencedVariablesTo(Collection<Variable> vars, Term t) {
		if (t instanceof Function) {
			addReferencedVariablesTo(vars, (Function)t);
		}
		else if (t instanceof Variable) {
			vars.add((Variable)t);
		}
		else /* (t instanceof BNode) || (t instanceof IRIConstant) || (t instanceof ValueConstant) */ {
			// no-op
		}
	}
	
}
