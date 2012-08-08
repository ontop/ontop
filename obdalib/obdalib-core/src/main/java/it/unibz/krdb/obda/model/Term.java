package it.unibz.krdb.obda.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * This class defines the basic component of the proposition. A proposition is a
 * particular kind of sentence, in which the subject and predicate are combined.
 * In this scenario, term means the subject (or sometimes can be the object) of
 * a preposition.
 */
public interface Term extends Serializable {

	
	public Term clone();
	
	public Set<Variable> getReferencedVariables();

	public Map<Variable, Integer> getVariableCount();
}
