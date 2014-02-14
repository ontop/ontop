package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.Set;

public interface EquivalencesDAG<T> extends Iterable<Equivalences<T>> {

	Equivalences<T> getVertex(T v);

	Set<Equivalences<T>> getDirectSub(Equivalences<T> v);

	/**
	 * Reflexive and transitive closure of the sub-description relation
	 * @param v: an equivalence set of a description (a property or a class)
	 * @return equivalence sets for all sub-descriptions (including v)
	 */
	
	Set<Equivalences<T>> getSub(Equivalences<T> v);

	Set<Equivalences<T>> getDirectSuper(Equivalences<T> v);

	/**
	 * Reflexive and transitive closure of the super-description relation
	 * @param v: an equivalence set of a description (a property or a class)
	 * @return equivalence sets for all super-descriptions (including v)
	 */
	Set<Equivalences<T>> getSuper(Equivalences<T> v);
	
	boolean isIndexed(Equivalences<T> v);
}