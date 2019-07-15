package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents intersections of classes or properties as 
 *     all the sub-classes (resp., sub-properties).
 * 
 * Such a representation makes containment checks fast.
 * 
 * @author Roman Kontchakov
 *
 * @param <T>
 */

public class Intersection<T> {

	/**
	 * downward saturated set 
	 * 		(contains all sub-class or sub-properties) 
	 * 
	 * null represents the maximal element -- top
	 * the empty set is the minimal element -- bottom
	 */
	private Set<T> elements; // initially is top

	/**
	 * default constructor -- the intersection equals to top
	 * 
	 */
	public Intersection() {
		elements = null;
	}
	
	/**
	 * construct from another intersection by copying the set
	 * 
	 * @param arg an intersection
	 */
	
	public Intersection(Intersection<T> arg) {
		elements = (arg.elements == null) ? null : new HashSet<>(arg.elements);
	}
	
	/**
	 * checks if the intersection is in fact the empty class or property
	 * 
	 * @return true if it is equivalent to bottom
	 */
	
	public boolean isBottom() {
		return (elements != null) && elements.isEmpty();
	}
	
	/**
	 * checks if the intersection is equivalent to top (i.e., contains all elements)
	 * 
	 * @return true if it is equivalent to top
	 */
	
	public boolean isTop() {
		return (elements == null);
	}
	
	/**
	 * checks if the intersection is entailed (subsumes) e
	 *  
	 * @param e a class or a property
	 *  
	 * @return true if e entails (is subsumed) by the intersection 
	 */
	
	public boolean subsumes(T e) {
		// top contains everything
		return (elements == null) || elements.contains(e);
	}
	
	/**
	 * modifies the intersection by further intersecting it with a class / property
	 * 
	 * IMPORTANT: the class / property is given by the DOWNWRD-SATURATED SET
	 *              (in other words, by the result of EquivalencesDAG.getSubRepresentatives
	 * 
	 * @param e a non-empty downward saturated set for class / property
	 */
	
	public void intersectWith(Collection<T> e) {

		if (elements == null) // we have top, the intersection is sub
			elements = new HashSet<>(e); // copy the set
		else
			elements.retainAll(e);
	}
	
	/**
	 * modifies by intersecting with another intersection
	 * 
	 * @param arg another intersection 
	 */
	
	public void intersectWith(Intersection<T> arg) {

		if (arg.elements != null) {  // if the argument is top then leave all as is
			if (arg.elements.isEmpty()) // if arg is empty, the result is empty
				elements = Collections.emptySet();
			else
				intersectWith(arg.elements);
		}
	}

	/**
	 * resets the intersection to the trivial case (top)
	 */
	public void setToTop() {
		elements = null;
	}

	/**
	 * empties the intersection 
	 */
	
	public void setToBottom() {
		elements = Collections.emptySet();
	}
		
	@Override
	public String toString() {
		return ((elements == null) ? "TOP" : elements.toString());
	}
	
}
