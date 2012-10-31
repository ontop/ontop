package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TreeWitnessGenerator {
	private final Property property;
	private final OClass filler;
	private Set<BasicClassDescription> concepts = new HashSet<BasicClassDescription>();
	private Set<BasicClassDescription> subconcepts;
	private PropertySomeRestriction existsRinv;
	private TreeWitnessReasonerLite reasoner;

	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	
	public TreeWitnessGenerator(TreeWitnessReasonerLite reasoner, Property property, OClass filler) {
		this.reasoner = reasoner;
		this.property = property;
		this.filler = filler;
	}

	void addConcept(BasicClassDescription con) {
		concepts.add(con);
	}
	
	public static Set<BasicClassDescription> getMaximalBasicConcepts(Collection<TreeWitnessGenerator> gens) {
		if (gens.size() == 1)
			return gens.iterator().next().concepts;
		
		Set<BasicClassDescription> cons = new HashSet<BasicClassDescription>();
		for (TreeWitnessGenerator twg : gens) {
			cons.addAll(twg.concepts);
		}
		
		if (cons.size() > 1) {
			// TODO: select only maximal ones
			//log.debug("MORE THAN ONE GEN CON: " + cons);
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
