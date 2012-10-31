package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinimalCQProducer {
	private final TreeWitnessReasonerLite reasoner;
	private final List<Term> freeVariables;
	private List<Atom> atoms = new LinkedList<Atom>();
	
	private static final OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	private static final Logger log = LoggerFactory.getLogger(MinimalCQProducer.class);	
	
	public MinimalCQProducer(TreeWitnessReasonerLite reasoner, List<Term> freeVariables) {
		this.reasoner = reasoner;
		this.freeVariables = freeVariables;
	}
	
	public boolean isMoreSpecific(Atom a1, Atom a2) {
		if (a1.equals(a2))
			return true;

		if ((a2.getArity() == 1) && (a1.getArity() == 1)) {
			if (a1.getTerm(0).equals(a2.getTerm(0))) {
					// add a2.term anonymous 
				Set<BasicClassDescription> subconcepts = reasoner.getSubConcepts(a2.getPredicate());
				if (subconcepts.contains(ontFactory.createClass(a1.getPredicate()))) {
					log.debug("" + a1 + " IS MORE SPECIFIC (1-1) THAN " + a2);
					return true;
				}
			}
		}
		else if ((a1.getArity() == 1) && (a2.getArity() == 2)) {
			Term a1term = a1.getTerm(0);
			if ((a2.getTerm(1) instanceof AnonymousVariable) && a2.getTerm(0).equals(a1term)) {
				PropertySomeRestriction prop = ontFactory.getPropertySomeRestriction(a2.getPredicate(), false);
				Set<BasicClassDescription> subconcepts = reasoner.getSubConcepts(prop);
				if (subconcepts.contains(ontFactory.createClass(a1.getPredicate()))) {
					log.debug("" + a1 + " IS MORE SPECIFIC (1-2) THAN " + a2);
					return true;
				}
			}
			else if ((a2.getTerm(0) instanceof AnonymousVariable) && a2.getTerm(1).equals(a1term)) {
				PropertySomeRestriction prop = ontFactory.getPropertySomeRestriction(a2.getPredicate(), true);
				Set<BasicClassDescription> subconcepts = reasoner.getSubConcepts(prop);
				if (subconcepts.contains(ontFactory.createClass(a1.getPredicate()))) {
					log.debug("" + a1 + " IS MORE SPECIFIC (1-2) THAN " + a2);
					return true;
				}
			}
		}
		else if ((a1.getArity() == 2) && (a2.getArity() == 1)) { // MOST USEFUL
			Term a2term = a2.getTerm(0);
			if (a1.getTerm(0).equals(a2term)) {
				PropertySomeRestriction prop = ontFactory.getPropertySomeRestriction(a1.getPredicate(), false);
				if (reasoner.getSubConcepts(a2.getPredicate()).contains(prop)) {
					log.debug("" + a1 + " IS MORE SPECIFIC (2-1) THAN " + a2);
					return true;
				}
			}
			if (a1.getTerm(1).equals(a2term)) {
				PropertySomeRestriction prop = ontFactory.getPropertySomeRestriction(a1.getPredicate(), true);
				if (reasoner.getSubConcepts(a2.getPredicate()).contains(prop)) {
					log.debug("" + a1 + " IS MORE SPECIFIC (2-1) THAN " + a2);
					return true;
				}
			}
		}
		return false;
	}
	
	public void add(Atom atom) {
		Iterator<Atom> i = atoms.iterator();
		while (i.hasNext()) {
			Atom a = i.next();
			if (isMoreSpecific(a, atom))
				return;
			if (isMoreSpecific(atom, a))
				i.remove();
		}
		atoms.add(atom);
	}

	public boolean wouldSubsume(Atom atom) {
		for (Atom a : atoms) 
			if (isMoreSpecific(a, atom))
				return true;
		return false;
	}
	
	public void addNoCheck(Atom atom) {
		atoms.add(atom);
	}
	
	public List<Atom> getBody() {
		return atoms;
	}
}
