package it.unibz.inf.ontop.spec.ontology;

import com.google.common.collect.ImmutableSet;

import java.util.stream.Stream;

public interface EquivalencesDAG<T> extends Iterable<Equivalences<T>> {

	Equivalences<T> getVertex(T v);

	/**
	 * 
	 * @param v
	 * @return null if either v is the representative itself or the DAG does not contain the vertex
	 */
	
	T getCanonicalForm(T v);
	
	ImmutableSet<Equivalences<T>> getDirectSub(Equivalences<T> v);

	/**
	 * Reflexive and transitive closure of the sub-description relation
	 * @param v: an equivalence set of a description (a property or a class)
	 * @return equivalence sets for all sub-descriptions (including v)
	 */
	
	ImmutableSet<Equivalences<T>> getSub(Equivalences<T> v);

	ImmutableSet<Equivalences<T>> getDirectSuper(Equivalences<T> v);

	ImmutableSet<T> getSubRepresentatives(T v);
	
	/**
	 * Reflexive and transitive closure of the super-description relation
	 * @param v: an equivalence set of a description (a property or a class)
	 * @return equivalence sets for all super-descriptions (including v)
	 */
	ImmutableSet<Equivalences<T>> getSuper(Equivalences<T> v);

	Stream<Equivalences<T>> stream();
}