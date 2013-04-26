package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TreeWitnessReasonerLite
 * 
 * a simple reasoner for DL-Lite that computes and gives 
 *       - subconcepts for a given concept 
 *       - subproperties for a given property
 * 
 * @author Roman Kontchakov
 *
 */


public class TreeWitnessReasonerLite {
	private Ontology tbox;

	// reflexive and transitive closure of the relations
	private Map<BasicClassDescription, Set<BasicClassDescription>> subconcepts; 
	private Map<Property, Set<Property>> subproperties; 

	// caching for predicate symbols 
	private Map<Predicate, Set<BasicClassDescription>> predicateSubconcepts;
	private Map<Predicate, Set<Property>> predicateSubproperties;
	private Map<Predicate, Set<Property>> predicateSubpropertiesInv;

	// tree witness generators of the ontology (i.e., positive occurrences of \exists R.B)
	private Collection<TreeWitnessGenerator> generators;

	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessReasonerLite.class);	

	public static final OClass owlThing = ontFactory.createClass("http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_Thing");	
	
	public OntologyFactory getOntologyFactory() {
		return ontFactory;
	}
	
	public void setTBox(Ontology ontology) {

		this.tbox = ontology;
		log.debug("SET ONTOLOGY {}", ontology);

		Map<ClassDescription, TreeWitnessGenerator> gens = new HashMap<ClassDescription, TreeWitnessGenerator>();
		subconcepts = new HashMap<BasicClassDescription, Set<BasicClassDescription>>();
		subproperties = new HashMap<Property, Set<Property>>();
		
		predicateSubconcepts = new HashMap<Predicate, Set<BasicClassDescription>>();
		predicateSubproperties = new HashMap<Predicate, Set<Property>>();
		predicateSubpropertiesInv = new HashMap<Predicate, Set<Property>>();
		
		// COLLECT GENERATING CONCEPTS (together with their declared subclasses)
		// COLLECT SUB-CONCEPT AND SUB-PROPERTY RELATIONS
		log.debug("AXIOMS");
		for (Axiom ax : tbox.getAssertions()) {
			if (ax instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl sax = (SubClassAxiomImpl) ax;
				log.debug("CI AXIOM: {}", sax);
				BasicClassDescription subConcept = (BasicClassDescription)sax.getSub();
				ClassDescription superConcept = sax.getSuper();
				if (superConcept instanceof PropertySomeClassRestriction) {
					PropertySomeClassRestriction some = (PropertySomeClassRestriction)superConcept;
					// make \exists R.\top equivalent to \exists R
					if (some.getFiller().equals(owlThing)) 
						superConcept = ontFactory.createPropertySomeRestriction(some.getPredicate(), some.isInverse());
					
					TreeWitnessGenerator twg = gens.get(superConcept);
					if (twg == null) {
						twg = new TreeWitnessGenerator(this, ontFactory.createObjectProperty(some.getPredicate().getName(), some.isInverse()), some.getFiller());			
						gens.put(superConcept, twg);
					}
					twg.addConcept(subConcept);
					log.debug("GENERATING CI: {} <= {}", subConcept, superConcept);
				}
				else {
					BasicClassDescription basicSuperConcept = (BasicClassDescription)superConcept;
					Set<BasicClassDescription> set = subconcepts.get(basicSuperConcept);
					if (set == null) {
						set = new HashSet<BasicClassDescription>();
						set.add(basicSuperConcept);  // keep it reflexive
						subconcepts.put(basicSuperConcept, set);
					}
					set.add(subConcept);	
					
					if (basicSuperConcept instanceof PropertySomeRestriction) {
						PropertySomeRestriction some = (PropertySomeRestriction)basicSuperConcept;
						TreeWitnessGenerator twg = gens.get(some);
						if (twg == null) {
							twg = new TreeWitnessGenerator(this, ontFactory.createObjectProperty(some.getPredicate().getName(), some.isInverse()), owlThing);			
							gens.put(superConcept, twg);
						}
						twg.addConcept(subConcept);
						log.debug("GENERATING CI: {} <= {}", subConcept, some);
					}
				}
			} 
			else if (ax instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl sax = (SubPropertyAxiomImpl) ax;
				log.debug("RI AXIOM: {}", sax);
				Property superProperty = sax.getSuper();
				Property subProperty = sax.getSub();
				Property superPropertyInv = ontFactory.createProperty(superProperty.getPredicate(), !superProperty.isInverse());
				Property subPropertyInv = ontFactory.createProperty(subProperty.getPredicate(), !subProperty.isInverse());
				Set<Property> set = subproperties.get(superProperty);
				Set<Property> setInv = null;
				if (set == null) {
					set = new HashSet<Property>(4);
					set.add(superProperty);  // keep it reflexive
					set.add(subProperty);
					subproperties.put(superProperty, set);
					setInv = new HashSet<Property>(4);
					setInv.add(superPropertyInv); // keep it reflexive
					setInv.add(subPropertyInv);
					subproperties.put(superPropertyInv, setInv);
				}
				else 
					setInv = subproperties.get(superPropertyInv);
				
				set.add(subProperty);
				setInv.add(subPropertyInv);
			}
			else
				log.debug("UNKNOWN AXIOM TYPE: {}", ax);
		}

		generators = gens.values();
	
		// SATURATE PROPERTY HIERARCHY
		{
			for (Map.Entry<Property, Set<Property>> p : subproperties.entrySet())
				log.debug("DECLARED SUBPROPERTIES OF {} ARE {}", p.getKey(), p.getValue());

			graphTransitiveClosure(subproperties);
			
			for (Map.Entry<Property, Set<Property>> p : subproperties.entrySet())
				log.debug("SATURATED SUBPROPERTY OF {} ARE {}", p.getKey(), p.getValue());
		}
	
		// SATURATE CONCEPT HIERARCHY
		{
			for (Map.Entry<BasicClassDescription, Set<BasicClassDescription>> k : subconcepts.entrySet())
				log.debug("DECLARED SUBCONCEPTS OF {} ARE {}", k.getKey(), k.getValue());
	
			// ADD INCLUSIONS BETWEEN EXISTENTIALS OF SUB-PROPERTIES
			for (Map.Entry<Property, Set<Property>> prop : subproperties.entrySet()) {
				PropertySomeRestriction existsSuper =  ontFactory.createPropertySomeRestriction(prop.getKey().getPredicate(), prop.getKey().isInverse());
				Set<BasicClassDescription> setExistsSuper = subconcepts.get(existsSuper);
				if (setExistsSuper == null) {
					setExistsSuper = new HashSet<BasicClassDescription>();
					// no need to insert reflexive pair as the role hierarchy is already reflexive
					subconcepts.put(existsSuper, setExistsSuper); 
				}
				for (Property subproperty : prop.getValue()) 
					setExistsSuper.add(ontFactory.createPropertySomeRestriction(subproperty.getPredicate(), subproperty.isInverse())); 	
			}
			graphTransitiveClosure(subconcepts);
			
			for (Map.Entry<BasicClassDescription, Set<BasicClassDescription>> k : subconcepts.entrySet())
				log.debug("SATURATED SUBCONCEPTS OF {} ARE {}", k.getKey(), k.getValue());
		}
	}
	
	private static <T> void graphTransitiveClosure(Map<T, Set<T>> graph) {
		log.debug("COMPUTING TRANSITIVE CLOSURE");
		Queue<T> useForExtension = new LinkedList<T>(graph.keySet());
		while (!useForExtension.isEmpty()) {
			T o1key = useForExtension.poll();
			//log.debug("   USE FOR EXTENSION: " + o1key);
			Set<T> o1value = null;
			for (Map.Entry<T, Set<T>> o2 : graph.entrySet()) {
				if (o2.getKey() == o1key)
					continue;
				if (o2.getValue().contains(o1key)) {
					if (o1value == null)
						o1value = graph.get(o1key);
					if (o2.getValue().addAll(o1value)) {
						useForExtension.add(o2.getKey());
						//log.debug("ALL " + o2.getKey() + " ARE EXTENDED WITH ALL " + o1key);
					}
				}
			}
		}		
	}
	
	/**
	 * getSubConcepts
	 * 
	 * @param con is a basic class description (concept name or \exists R)
	 * @return the set of subsconcepts of B
	 */
	
	public Set<BasicClassDescription> getSubConcepts(BasicClassDescription con) {
		Set<BasicClassDescription> s = subconcepts.get(con);
		if (s == null) {
			s = Collections.singleton(con);
			subconcepts.put(con, s);
		}
		return s;
	}

	public Set<BasicClassDescription> getSubConcepts(Predicate pred) {
		Set<BasicClassDescription> s = predicateSubconcepts.get(pred);
		if (s == null) {
			s = getSubConcepts(ontFactory.createClass(pred));
			predicateSubconcepts.put(pred, s);
		}
		return s;
	}

	public IntersectionOfConceptSets getSubConcepts(Collection<Function> atoms) {
		IntersectionOfConceptSets subc = new IntersectionOfConceptSets();
		for (Function a : atoms) {
			 if (a.getArity() != 1)
				 return IntersectionOfConceptSets.EMPTY;   // binary predicates R(x,x) cannot be matched to the anonymous part

			 if (!subc.intersect(getSubConcepts(a.getPredicate())))
				 return IntersectionOfConceptSets.EMPTY;
		}
		return subc;
	}
	
	
	/**
	 *  getSubproperties
	 *  
	 * @param prop is a property (property name or its inverse)
	 * @return the set of subproperties
	 */
	
	public Set<Property> getSubProperties(Property prop) {
		Set<Property> s = subproperties.get(prop);
		if (s == null) {
			s = Collections.singleton(prop);
			subproperties.put(prop, s);
		}
		return s;
	}
	
	public Set<Property> getSubProperties(Predicate pred, boolean inverse) {
		Map<Predicate, Set<Property>> cache = (inverse ? predicateSubpropertiesInv : predicateSubproperties);
		Set<Property> s = cache.get(pred);
		if (s == null) {
			s = getSubProperties(ontFactory.createProperty(pred, inverse));
			cache.put(pred, s);
		}
		return s;
	}
	
	/**
	 * getGenerators
	 * 
	 * @return the collection of all tree witness generators for the ontology
	 */
	
	public Collection<TreeWitnessGenerator> getGenerators() {
		return generators;
	}
	
	/**
	 * getSubConcepts
	 * 
	 * @param twgs a set of tree witness generators
	 * @return the set of all sub-concepts for all of the tree witness generators
	 */
	
	
	public static Set<BasicClassDescription> getSubConceptsForGenerators(Collection<TreeWitnessGenerator> twgs) {
		if (twgs.size() == 1)
			return twgs.iterator().next().getSubConcepts();
		
		Set<BasicClassDescription> all = new HashSet<BasicClassDescription>();		
		for (TreeWitnessGenerator twg : twgs) 
			all.addAll(twg.getSubConcepts());
		return all;
	}
	
	
	/**
	 * computes intersections of sets of properties
	 * 
	 * internal representation: the downward-saturated set of properties (including all sub-properties)
	 * 
	 * @author roman
	 *
	 */
	
	public static class IntersectionOfProperties {
		private Set<Property> set;
		
		public IntersectionOfProperties() {
		}
		
		public IntersectionOfProperties(Set<Property> set2) {
			set = (set2 == null) ? null : new HashSet<Property>(set2);
		}
		
		public boolean intersect(Set<Property> subp) {
			if (set == null) // first atom
				set = new HashSet<Property>(subp);
			else
				set.retainAll(subp);
			
			if (set.isEmpty()) {
				set = Collections.EMPTY_SET;
				return false;
			}
			else
				return true;
		}
		
		public void clear() {
			set = null;
		}
		
		public Set<Property> get() {
			return set;
		}
		
		@Override
		public String toString() {
			return ((set == null) ? "properties TOP" : "properties " + set.toString());
		}
	}
	
	
	
	
	public static class IntersectionOfConceptSets {
		public static final IntersectionOfConceptSets EMPTY = new IntersectionOfConceptSets(Collections.EMPTY_SET);
		
		private Set<BasicClassDescription> set;
		
		public IntersectionOfConceptSets() {			
		}

		public IntersectionOfConceptSets(Set<BasicClassDescription> subc) {			
			set = (subc == null) ?  null : (subc.isEmpty() ? Collections.EMPTY_SET : new HashSet<BasicClassDescription>(subc));
		}

		public IntersectionOfConceptSets(IntersectionOfConceptSets s2) {
			this(s2.set);
		}
		
		public boolean intersect(IntersectionOfConceptSets subc) {
			// null denotes \top
			if (subc.set == null) 
				return !isEmpty();
			
			return intersect(subc.set);
		}	

		public boolean intersect(Set<BasicClassDescription> subc) {
			// intersection with the empty set is empty
			if (subc.isEmpty()) {
				set = Collections.EMPTY_SET;
				return false;
			}

			if (set == null) { // the intersection has not been initialised
				set = new HashSet<BasicClassDescription>(subc);
				return true;
			}

			set.retainAll(subc);
			if (set.isEmpty()) {
				set = Collections.EMPTY_SET;
				return false;
			}
			return true;
		}	
		
		public boolean isEmpty() {
			return ((set != null) && set.isEmpty());
		}
		
		public Set<BasicClassDescription> get() {
			return set;
		}
		
		public void clear() {
			set = null;
		}
	}	
}
