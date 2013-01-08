package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessGenerator {
	private final Property property;
	private final OClass filler;
	private Set<BasicClassDescription> concepts = new HashSet<BasicClassDescription>();
	private Set<BasicClassDescription> subconcepts;
	private PropertySomeRestriction existsRinv;
	private TreeWitnessReasonerLite reasoner;

	private final OntologyFactory ontFactory; 
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessGenerator.class);	
	
	public TreeWitnessGenerator(TreeWitnessReasonerLite reasoner, Property property, OClass filler) {
		this.reasoner = reasoner;
		this.property = property;
		this.filler = filler;
		this.ontFactory = reasoner.getOntologyFactory();
	}

	void addConcept(BasicClassDescription con) {
		concepts.add(con);
	}
	
	public static Set<BasicClassDescription> getMaximalBasicConcepts(Collection<TreeWitnessGenerator> gens, TreeWitnessReasonerLite reasoner) {
		if (gens.size() == 1)
			return gens.iterator().next().concepts;
		
		Set<BasicClassDescription> cons = new HashSet<BasicClassDescription>();
		for (TreeWitnessGenerator twg : gens) {
			cons.addAll(twg.concepts);
		}
		
		if (cons.size() > 1) {
			// TODO: re-implement as removals (inner iterator loop with restarts on top) 
			log.debug("MORE THAN ONE GEN CON: " + cons);
			Set<BasicClassDescription> reduced = new HashSet<BasicClassDescription>();
			for (BasicClassDescription concept0 : cons) {
				boolean found = false;
				for (BasicClassDescription concept1 : cons)
					if (!concept0.equals(concept1) && reasoner.getSubConcepts(concept1).contains(concept0)) {
						found = true;
						break;
					}
				if (!found)
					reduced.add(concept0);
			}
			cons = reduced;
		}
		
		return cons;
	}
	
	
	public Set<BasicClassDescription> getSubConcepts() {
		if (subconcepts == null) {
			subconcepts = new HashSet<BasicClassDescription>();
			for (BasicClassDescription con : concepts)
				subconcepts.addAll(reasoner.getSubConcepts(con));
		}
		return subconcepts;
	}
	
	
	public Property getProperty() {
		return property;
	}
	
	public boolean endPointEntailsAnyOf(Set<BasicClassDescription> subc) {
		if (existsRinv == null)
			existsRinv = ontFactory.createPropertySomeRestriction(property.getPredicate(), !property.isInverse());	
		
		return subc.contains(existsRinv) || subc.contains(filler);
	}
	
	@Override 
	public String toString() {
		return "tw-generator E" + property.toString() + "." + filler.toString();
	}
	
	@Override
	public int hashCode() {
		return property.hashCode() ^ filler.hashCode();
	}
	
	@Override 
	public boolean equals(Object other) {
		if (other instanceof TreeWitnessGenerator) {
			TreeWitnessGenerator o = (TreeWitnessGenerator)other;
			return (this.property.equals(o.property) && this.filler.equals(o.filler));		
		}
		return false;
	}
}
