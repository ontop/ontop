package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.ontology.Description;

import java.util.Set;

/***
 * A class that provides TBox entailment computation based on a DAG
 * 
 * @author mariano
 * 
 */
public interface TBoxDAG {

	public Set<Set<Description>> getDirectChildren(Description desc);

	public Set<Set<Description>> getDirectParents(Description desc);

	public Set<Set<Description>> getDescendants(Description desc);

	public Set<Set<Description>> getAncestors(Description desc);

	public Set<Description> getEquiavlences(Description description);

	public void getNode();

}
