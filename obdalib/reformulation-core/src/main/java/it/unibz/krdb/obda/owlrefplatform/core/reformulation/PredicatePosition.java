package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Predicate;

public class PredicatePosition implements Cloneable {
	
	private Predicate predicate;
	private int position;
	
	public PredicatePosition(Predicate predicate, int position)
	{
		this.predicate = predicate;
		this.position = position;
	}
	
	public boolean isInverseOf(PredicatePosition pos)
	{
		// handles only binary predicates
		return (this.predicate.equals(pos.predicate) && (this.position != pos.position));
	}

	public Predicate getPredicate()
	{
		return predicate;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public boolean equals(Object obj)
	{
		if ((obj == null) || !(obj instanceof PredicatePosition))
			return false;
		
		PredicatePosition pos = (PredicatePosition)obj;
		return (this.predicate.equals(pos.predicate) && (this.position == pos.position));
	}
	
	public String toString()
	{
		return predicate  + ((position == 1) ? "-" : "");
	}
}
