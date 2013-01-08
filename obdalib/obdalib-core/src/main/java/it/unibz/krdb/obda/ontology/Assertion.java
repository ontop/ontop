package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

/***
 * ABox assertions a.k.a. intentional axioms. Data assertions.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public interface Assertion extends Axiom {

	public int getArity();
	
	public Predicate getPredicate();

}
