package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.PredicateAtom;
import it.unibz.krdb.obda.model.Term;

public class TreeWitness {

	private Term term;
	private PredicatePosition direction;
	private boolean exists;
	private Map<Term, Vector<PredicatePosition> > func;
	
	Logger	log = LoggerFactory.getLogger(TreeWitness.class);
	
	public TreeWitness(Term term, PredicatePosition direction) 	{
		this.term = term;
		this.direction = direction;
		this.exists = true;
		this.func = new Hashtable<Term, Vector<PredicatePosition> >();
		func.put(term, new Vector<PredicatePosition>());
	}

	public String toString() {
		
		String description = "tree witness for (" + term + ", " + direction + ") ";
		String representation = "";
		if (!exists)
			representation = "DOES NOT EXIST";
		else {
			for (Term t: func.keySet()) {
				representation += (" " + t.toString() + "=" + func.get(t).toString()); 
			}
		}
		return description + representation;
	}
	
	public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof TreeWitness))
			return false;
		
		TreeWitness tw = (TreeWitness)obj;
		return (tw.getRoots().contains(getTerm()) && tw.getDirection().equals(getDirection()));								
	}
	
	public int hashCode() {
		return getDirection().getPredicate().getName().hashCode();
	}
	
	// returns true if the tree witness has been updated (and false otherwise)
	
	private boolean updateTermLabel(Term t, Vector<PredicatePosition> label) {
		if (func.containsKey(t)) {
			if (func.get(t).equals(label))
				return false;
			else {
				// log.debug("DISTINCT LABELS: " + func.get(t) + " and " + label);
				exists  = false;
			}
		}
		else 
			func.put(t, label);
		
		return true;
	}
	
	// returns true if the tree witness has been updated (and false otherwise)
	
	private boolean extendWithAtom(PredicatePosition pos, Term t1, Term t2) {
		//log.debug("extend with atom {}", pos + " (" + t1 + ", " + t2 + ")");
		Vector<PredicatePosition> l2 = func.get(t1);
		if (l2.isEmpty())
		{
			//log.debug("t2 label is empty");
			if (this.direction.equals(pos)) {
				Vector<PredicatePosition> lbl = new Vector<PredicatePosition>();
				lbl.add(pos);
				//log.debug("new label for " + t2 + " is " + lbl);
				return updateTermLabel(t2,lbl);
			}
		}
		else if (l2.lastElement().isInverseOf(pos)) {
			//log.debug("t2 label ends with the inverse: {}", l2);
			Vector<PredicatePosition> lbl = (Vector<PredicatePosition>)l2.clone();
			lbl.removeElementAt(lbl.size() - 1); // remove last
			//log.debug("new label for " + t2 + " is " + lbl);
			return updateTermLabel(t2,lbl);							
		}
		else {
			//log.debug("t2 label does not end with the inverse {}", l2);
			Vector<PredicatePosition> lbl = (Vector<PredicatePosition>)l2.clone();
			lbl.add(pos); // add extra one
			//log.debug("new label for " + t2 + " is " + lbl);
			return updateTermLabel(t2,lbl);													
		}	
		return false;
	}
	
	public boolean isInDomain(Term t) {
		return func.containsKey(t);
	}
	
	public Term getTerm() {
		return term;
	}
	
	public PredicatePosition getDirection() {
		return direction;
	}
	
	public List<Term> getRoots() {
		Vector<Term> roots = new Vector<Term>();
		for (Term t: func.keySet()) {
			if (func.get(t).size() == 0)
				roots.add(t);
		}
		return roots;
	}

	public List<Term> getNonRoots() {
		Vector<Term> nonroots = new Vector<Term>();
		for (Term t: func.keySet()) {
			if (func.get(t).size() > 0)
				nonroots.add(t);
		}
		return nonroots;
	}

	public boolean extendWithAtoms(List<Atom> atoms) {
		boolean changed = true;
		while (changed && exists) {
			changed = false;
			for(Atom a0: atoms) {
				PredicateAtom a = (PredicateAtom)a0;
				if (a.getArity() == 2) {
					Term t1 = a.getTerms().get(0);
					Term t2 = a.getTerms().get(1);
					if (func.containsKey(t1)) {
						changed = extendWithAtom(new PredicatePosition(a.getPredicate(), 2), t1, t2) || changed;						
						if (!exists)
							return exists;
					}
					if (func.containsKey(t2)) {
						changed = extendWithAtom(new PredicatePosition(a.getPredicate(), 1), t2, t1) || changed;
						if (!exists)
							return exists;
					}
				}
			}
		}
		return exists;			
	}
	
	// returns null if t is a root
	//     and the tail of the label otherwise
	public PredicatePosition getLabelTail(Term t) {
		Vector<PredicatePosition> label = func.get(t);
		if (label.isEmpty())
			return null;
		else
			return label.lastElement();
	}

	// returns true if the term is a root
	public boolean isRoot(Term t) 	{
		return (getLabelTail(t) == null);
	}
	
}
