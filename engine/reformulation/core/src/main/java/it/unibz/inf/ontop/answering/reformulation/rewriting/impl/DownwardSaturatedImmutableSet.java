package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Objects;
import java.util.stream.Collector;

/**
 * Represents intersections of classes or properties as 
 *     all the subclasses (resp., sub-properties).
 * 
 * Such a representation makes containment checks fast.
 * 
 * @author Roman Kontchakov
 *
 * @param <T>
 */

public class DownwardSaturatedImmutableSet<T> {

	/**
	 * downward-saturated set
	 * null represents the maximal element -- top
	 * the empty set is the minimal element -- bottom
	 */
	private final ImmutableSet<T> elements;

	private DownwardSaturatedImmutableSet(ImmutableSet<T> elements) {
		this.elements = elements;
	}

	public boolean isTop() {
		return elements == null;
	}

	public boolean isBottom() {
		return elements != null && elements.isEmpty();
	}

	/**
	 * checks if the intersection is entailed (subsumes) e
	 *  
	 * @param e a class or a property
	 *  
	 * @return true if e entails (is subsumed) by the intersection 
	 */
	
	public boolean subsumes(T e) {
		return isTop() || elements.contains(e);
	}

	/**
	 * intersection of two downward saturated sets
	 *
	 * @param i1
	 * @param i2
	 */
	
	public static <T> DownwardSaturatedImmutableSet<T> intersectionOf(DownwardSaturatedImmutableSet<T> i1, DownwardSaturatedImmutableSet<T> i2) {
		if (i1.isTop())
			return i2;

		if (i2.isTop())
			return i1;

		Sets.SetView<T> result = Sets.intersection(i1.elements, i2.elements);
		if (result.isEmpty())
			return bottom();

		return new DownwardSaturatedImmutableSet<>(result.immutableCopy());
	}

	public static <T> DownwardSaturatedImmutableSet<T> create(ImmutableSet<T> e) {
		return new DownwardSaturatedImmutableSet<>(e);
	}

	public static <T> DownwardSaturatedImmutableSet<T> top() {
		return TOP;
	}

	public static <T> DownwardSaturatedImmutableSet<T> bottom() {
		return BOTTOM;
	}

	private static final DownwardSaturatedImmutableSet BOTTOM = new DownwardSaturatedImmutableSet<>(ImmutableSet.of());
	private static final DownwardSaturatedImmutableSet TOP = new DownwardSaturatedImmutableSet<>(null);

	private final static class Accumulator<T> {
		private DownwardSaturatedImmutableSet<T> r = top();

		Accumulator<T> intersectWith(DownwardSaturatedImmutableSet<T> i) { r = intersectionOf(r, i); return this; }
		Accumulator<T> intersectWith(Accumulator<T> a) { r = intersectionOf(r, a.r); return this; }

		DownwardSaturatedImmutableSet<T> result() { return r; }
	}


	public static <T> Collector<DownwardSaturatedImmutableSet<T>, Accumulator<T>, DownwardSaturatedImmutableSet<T>> toIntersection() {
		return Collector.of(
				Accumulator::new, // Supplier
				Accumulator::intersectWith, // Accumulator
				Accumulator::intersectWith, // Merger
				Accumulator::result, // Finisher
				Collector.Characteristics.UNORDERED);
	}


	@Override
	public String toString() {
		return isTop() ? "TOP" : elements.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof DownwardSaturatedImmutableSet) {
			DownwardSaturatedImmutableSet<?> other = (DownwardSaturatedImmutableSet<?>)o;
			return Objects.equals(this.elements, other.elements);
		}
		return false;
	}
}
