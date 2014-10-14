package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents intersections of concepts or properties as 
 *     all the sub-concepts (resp., sub-properties).
 * 
 * Such a representation makes containment checks fast.
 * 
 * @author roman
 *
 * @param <T> BasicConceptDescription or Property
 */

public class Intersection<T> {

	/**
	 * downward saturated set 
	 * 		(contains all sub-concepts or sub-properties) 
	 * 
	 * null represents the maximal element -- top
	 * the empty set is the minimal element -- bottom
	 */
	private Set<T> elements; // initially is top
	
	public Intersection() {
		elements = null;
	}
	
	public Intersection(Intersection<T> arg) {
		if (arg.elements == null)
			elements = null;
		else
			elements = new HashSet<T>(arg.elements);
	}
	
	public boolean isBottom() {
		return (elements != null) && elements.isEmpty();
	}
	
	public boolean isTop() {
		return (elements == null);
	}
	
	public boolean contains(T e) {
		// top contains everything
		return (elements == null) || elements.contains(e);
	}
	
	/**
	 * 
	 * @param sub a non-empty downward saturated set 
	 */
	
	public void intersectWith(Set<T> sub) {
		
		if (elements == null) // we have top, the intersection is sub
			elements = new HashSet<T>(sub); // copy the set
		else
			elements.retainAll(sub);			
	}
	
	/**
	 * 
	 * @param arg another intersection 
	 */
	
	public void intersectWith(Intersection<T> arg) {
		// if the argument is top then leave all as is
		if (arg.elements != null) {
			
			// if arg is empty, the result is empty
			if (arg.elements.isEmpty())
				elements = Collections.emptySet();
			else
				intersectWith(arg.elements);	
		}
	}

	public void setToTop() {
		elements = null;
	}

	public void setToBottom() {
		elements = Collections.emptySet();
	}
	
	@Deprecated
	public Set<T> get() {
		return elements;
	}
	
	@Override
	public String toString() {
		return ((elements == null) ? "TOP" : elements.toString());
	}
	
}
