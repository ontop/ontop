package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

import java.io.Serializable;
import java.util.Set;

/***
 * A logical axiom.
 */
public interface Axiom  extends Serializable{

	public Set<Predicate> getReferencedEntities();
}
