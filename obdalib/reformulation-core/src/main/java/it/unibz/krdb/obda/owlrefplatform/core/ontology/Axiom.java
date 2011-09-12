package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;

import java.util.Set;

/***
 * A logical axiom
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public interface Axiom {

	public Set<Predicate> getReferencedEntities();

}
