package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.DisjointBasicClassAxiom;
import it.unibz.krdb.obda.ontology.DisjointDescriptionAxiom;
import it.unibz.krdb.obda.ontology.DisjointPropertyAxiom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeDataTypeRestriction;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class add facts to the datalog program regarding owl sparql entailments.
 * It is possible to activate or deactivate this feature in Quest changing the
 * preferences of QuestPreferences.SPARQL_OWL_ENTAILMENT
 * 
 */

//TODO Optimize nested for with caches

public class OWLEntailmentsToFactRule {

	private static Logger log = LoggerFactory.getLogger(OWLEntailmentsToFactRule.class);

	private static OBDADataFactory factory = OBDADataFactoryImpl.getInstance();

	private static OntologyFactory ontoFactory = OntologyFactoryImpl.getInstance();

	private static DatalogProgram program;
	
	static TBoxReasoner reasoner;

	/**
	 * Add facts to the datalog program regarding owl sparql entailments.
	 * 
	 * @param p
	 *            the datalog program used later by the unfolder
	 * @param onto
	 *            the ontology given by the user (starting .owl file)
	 * @param equivalenceMaps 
	 * 
	 */

	public static void addFacts(DatalogProgram p, Ontology onto, Map<Predicate, Description> equivalenceMaps) {

		program = p;

		reasoner = new TBoxReasonerImpl(onto);

		log.info("addEntailmentsClass");
		// add owl:equivalentClass, rdfs:subClassOf
		addEntailmentsForClasses();

		log.info("addEntailmentsProperty");
		// add owl:inverseOf, owl:equivalentProperty, rdfs:subPropertyOf
		addEntailmentsForProperties();

		log.info("addDisjointWith");
		// add owl:disjointWith
		addDisjointClasses(onto.getDisjointBasicClassAxioms());

		log.info("propertyDisjointWith");
		// add owl:propertyDisjointWith
		addDisjointProperties(onto.getDisjointPropertyAxioms());

		log.info("range");
		// rdfs:range, rdfs:domain
		addRangesAndDomainsFromClasses();

	}

	/**
	 * Add subclasses and equivalences in the datalog program using the DAG with classes. 
	 * 
	 */

	private static void addEntailmentsForClasses() {

		EquivalencesDAG<BasicClassDescription> dag = reasoner.getClasses();
		Iterator<Equivalences<BasicClassDescription>> it = dag.iterator();

		Predicate subClassOf = OBDAVocabulary.RDFS_SUBCLASS;

		while (it.hasNext()) {

			Equivalences<BasicClassDescription> eqv = it.next();

			Iterator<BasicClassDescription> iteq = eqv.getMembers().iterator();

			while (iteq.hasNext()) {

				BasicClassDescription classItem = iteq.next();

				/*
				 * Add the equivalences owl:equivalentClass for each node in the
				 * graph
				 */
				getEquivalencesClasses(classItem, eqv.getMembers());

				// iterate through all subclasses not only the direct ones
				Iterator<Equivalences<BasicClassDescription>> classesIt = dag.getSub(eqv).iterator();

				while (classesIt.hasNext()) {

					Equivalences<BasicClassDescription> eqq = classesIt.next();
					Iterator<BasicClassDescription> itcl = eqq.getMembers().iterator();

					while (itcl.hasNext()) {

						BasicClassDescription subClassItem = itcl.next();

						/*
						 * Add to datalog the subclasses rdfs:subclass, create a
						 * blank node if some property of description
						 */

						addBlankNodesRule(subClassItem, classItem, subClassOf);

					}

				}
			}
		}

	}
	

	/**
	 * Called by { @link #addEntailmentsForClasses(TBoxReasoner) } add the
	 * owl:equivalentClass 
	 * 
	 * @param equivalentMembers
	 *            set of equivalent descriptions (classes, data-properties and
	 *            properties some description)
	 */

	private static void getEquivalencesClasses(BasicClassDescription description, Set<BasicClassDescription> equivalentMembers) {

		Iterator<BasicClassDescription> itEquivalences = equivalentMembers.iterator();
		Predicate equivalentClass = OBDAVocabulary.OWL_EQUIVALENT_CLASS;

		while (itEquivalences.hasNext()) {

			BasicClassDescription node2 = itEquivalences.next();

			/*
			 * Add to datalog program equivalences owl:equivalentClass create a
			 * blank node if some property of description
			 */
			addBlankNodesRule(description, node2, equivalentClass);

		}

	}

	/**
	 * Add subproperties, equivalences and inverses in the datalog program using the DAG with properties.
	 *
	 */

	private static void addEntailmentsForProperties() {

		EquivalencesDAG<Property> dag = reasoner.getProperties();
		Iterator<Equivalences<Property>> it = dag.iterator();

		Predicate subPropertyOf = OBDAVocabulary.RDFS_SUBPROPERTY;

		while (it.hasNext()) {

			Equivalences<Property> eqv = it.next();

			Iterator<Property> iteq = eqv.getMembers().iterator();

			while (iteq.hasNext()) {

				Property propertyItem = iteq.next();

				// add to datalog program owl:inversesOf and
				// owl:equivalentProperty

				addInversesAndEquivalences(propertyItem, eqv.getMembers());

				// add all subproperty not only the direct ones
				Iterator<Equivalences<Property>> propertiesIt = dag.getSub(eqv).iterator();

				while (propertiesIt.hasNext()) {

					Equivalences<Property> eqq = propertiesIt.next();
					Iterator<Property> itcl = eqq.getMembers().iterator();

					while (itcl.hasNext()) {

						Property subPropertyItem = itcl.next();

						/*
						 * Add to datalog program rdfs:subproperty
						 */
						addNodesRule(subPropertyItem.toString(), propertyItem.toString(), subPropertyOf);

					}
				}
			}

		}

	}
	
	/**
	 * Called by { @link #addEntailmentsForProperties(TBoxReasoner) } 
	 * Add inverses and equivalences of properties to datalog program
	 * 
	 * @param equivalentMembers
	 */
	private static void addInversesAndEquivalences(Property property1, Set<Property> equivalentMembers) {

		Predicate inverseOf = OBDAVocabulary.OWL_INVERSE;
		Predicate equivalent = OBDAVocabulary.OWL_EQUIVALENT_PROPERTY;

		Iterator<Property> properties = equivalentMembers.iterator();

		while (properties.hasNext()) {

			Property propertyItem2 = properties.next();

			/*
			 * Add owl:equivalentProperty to the datalog program
			 */
			addNodesRule(property1.toString(), propertyItem2.toString(), equivalent);

			Property inverse;

			// add the inverse of the equivalent node
			if (propertyItem2.isInverse()) {

				inverse = ontoFactory.createProperty(propertyItem2.getPredicate(), false);
			}
			else {

				inverse = ontoFactory.createProperty(propertyItem2.getPredicate(), true);
			}

			/*
			 * Add owl:inverseOf to the datalog program
			 */
			addNodesRule(property1.toString(), inverse.toString(), inverseOf);
		}

	}

	/**
	 * From the given ontology and the DAG with classes, add the facts for owl:disjointWith to datalog program
	 * 
	 * @param set
	 *            set of disjoint axiom with predicates of class and property some description. It does not contain inverses
	 */

	private static void addDisjointClasses(Set<DisjointBasicClassAxiom> set) {

		Predicate disjointClass = OBDAVocabulary.OWL_DISJOINT_CLASS;

		EquivalencesDAG<BasicClassDescription> dag = reasoner.getClasses();

		for (DisjointBasicClassAxiom disjointElements : set) {

			for (Predicate predicate1 : disjointElements.getReferencedEntities()) {

				BasicClassDescription class1 = null;

				if (predicate1.isClass()) {

					class1 = ontoFactory.createClass(predicate1);
				}
				else {

					class1 = ontoFactory.createPropertySomeRestriction(predicate1, false);
				}

				Equivalences<BasicClassDescription> equivalentDescriptions = dag.getVertex(class1);

				// add disjoints considering equivalent nodes and subclasses
				Set<Equivalences<BasicClassDescription>> classes = dag.getSub(equivalentDescriptions);

				for (Equivalences<BasicClassDescription> equivalentclasses : classes) {

					for (BasicClassDescription description1 : equivalentclasses.getMembers()) {

						for (Predicate predicate2 : disjointElements.getReferencedEntities()) {

							// a class cannot be disjoint with itself or one of
							// its equivalent classes
							if (!predicate1.equals(predicate2)) {

								BasicClassDescription class2 = null;

								if (predicate2.isClass()) {

									class2 = ontoFactory.createClass(predicate2);
								}
								else {

									class2 = ontoFactory.createPropertySomeRestriction(predicate2, false);
								}

								Equivalences<BasicClassDescription> equivalentDescriptions2 = dag.getVertex(class2);

								Set<Equivalences<BasicClassDescription>> classes2 = dag.getSub(equivalentDescriptions2);

								for (Equivalences<BasicClassDescription> equivalentclasses2 : classes2) {

									for (BasicClassDescription description2 : equivalentclasses2.getMembers()) {

										/*
										 * Add owl:disjointWith facts to the
										 * datalog program
										 */
										addBlankNodesRule(description1, description2, disjointClass);
									}

								}

							}
						}

					}
				}
			}

		}
	}
	
	/**
	 * From the given ontology and the DAG with properties, add the facts for owl:propertyDisjointWith
	 * 
	 * @param set
	 *            set of disjoint axiom with predicates of properties. It does not contain inverses.
	 */
	
	private static void addDisjointProperties(Set<DisjointPropertyAxiom> set) {

		Predicate disjointProperty = OBDAVocabulary.OWL_DISJOINT_PROPERTY;

		EquivalencesDAG<Property> dag = reasoner.getProperties();

		for (DisjointPropertyAxiom disjointElements : set) {

			for (Predicate predicate1 : disjointElements.getReferencedEntities()) {

				Property prop1 = ontoFactory.createProperty(predicate1, false);

				Equivalences<Property> equivalentDescriptions = dag.getVertex(prop1);

				// add disjoints considering equivalent nodes and subproperties
				Set<Equivalences<Property>> properties = dag.getSub(equivalentDescriptions);

				for (Equivalences<Property> equivalentproperties : properties) {

					for (Property description1 : equivalentproperties.getMembers()) {

						for (Predicate predicate2 : disjointElements.getReferencedEntities()) {

							// a property cannot be disjoint with itself or one
							// of its equivalent properties
							if (!predicate1.equals(predicate2)) {

								Property prop2 = ontoFactory.createProperty(predicate2, false);

								for (Property description2 : dag.getVertex(prop2)) {

									/*
									 * Add owl:propertyDisjointWith facts to the
									 * datalog program
									 */

									addNodesRule(description1.toString(), description2.toString(), disjointProperty);

									Property inverseDescription1 = ontoFactory.createProperty(description1.getPredicate(), true);
									Property inverseDescription2 = ontoFactory.createProperty(description2.getPredicate(), true);

									/*
									 * Add owl:propertyDisjointWith facts for
									 * the inverses to the datalog program
									 */
									addNodesRule(inverseDescription1.toString(), inverseDescription2.toString(), disjointProperty);

								}
							}

						}
					}
				}
			}

		}
	}
	
	/**
	 * Add rdfs:domain and rdfs:range. The domain of a property P is given by the direct named (OClass)
	 * superclass C of the property some description existsP, the range of a property is given by the domain of the inverse
	 * The method iterates through the DAG with classes. It first search between
	 * the equivalent nodes (if they are present)  and then it searches in the
	 * direct superclasses or subclasses.
	 * 
	 */
	
	private static void addRangesAndDomainsFromClasses() {

		boolean foundRange; // if true we found the range or domain between the
							// equivalent classes

		EquivalencesDAG<BasicClassDescription> dag = reasoner.getClasses();
		Iterator<Equivalences<BasicClassDescription>> it = dag.iterator();

		while (it.hasNext()) {

			foundRange = false;

			Equivalences<BasicClassDescription> eqv = it.next();

			Iterator<BasicClassDescription> itEquivalences = eqv.getMembers().iterator();

			while (itEquivalences.hasNext()) {

				BasicClassDescription node1 = itEquivalences.next();
				Iterator<BasicClassDescription> itEquivalences2 = eqv.getMembers().iterator();

				while (itEquivalences2.hasNext()) {

					BasicClassDescription node2 = itEquivalences2.next();

					if (!node1.equals(node2)) {

						/*
						 * add rdfs:range and rdfs:domain to datalog program
						 * considering also inverses
						 */
						if (addRangeOrDomainRule(node1, node2)) {
							foundRange = true;
						}
					}

				}

			}

			/*
			 * Search between superclasses the domain and rangeif there were no
			 * named classes in the equivalent nodes.
			 */
			if (!foundRange) {

				Iterator<BasicClassDescription> iteq = eqv.getMembers().iterator();

				while (iteq.hasNext()) {

					BasicClassDescription classItem = iteq.next();

					if (classItem instanceof PropertySomeRestriction) {

						Iterator<Equivalences<BasicClassDescription>> classesIt2 = dag.getDirectSuper(eqv).iterator();

						while (classesIt2.hasNext()) {

							Equivalences<BasicClassDescription> eqq = classesIt2.next();
							Iterator<BasicClassDescription> itcl = eqq.getMembers().iterator();

							while (itcl.hasNext()) {

								BasicClassDescription superClassItem = itcl.next();

								/*
								 * add rdfs:range and rdfs:domain to datalog
								 * program considering also inverses
								 */
								addRangeOrDomainRule(classItem, superClassItem);

							}

						}
					}
				}
			}
		}

	}
	
	/**
	 * Add rdfs:range and rdfs:domain facts to datalog program
	 * It assigns range and domain both to the property and its inverse
	 * 
	 * @param subDescription
	 *            is analyzed if it is a property some restriction
	 * @param description
	 *            is analyzed if it is a class
	 */

	private static boolean addRangeOrDomainRule(BasicClassDescription subDescription, BasicClassDescription description) {

		List<Term> terms = new ArrayList<Term>();

		if (subDescription instanceof PropertySomeRestriction) {
			if (!(description instanceof PropertySomeRestriction)) {
				// get the predicate
				Predicate predicate1 = ((PropertySomeRestriction) subDescription).getPredicate();
				// get the property as inverse
				Property inverse = ontoFactory.createProperty(predicate1, true);

				// we assign the domain of the property and the range of its
				// inverse
				if (((PropertySomeRestriction) subDescription).isInverse())
				{
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(inverse.toString())));
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(description.toString())));
					Function head = factory.getFunction(OBDAVocabulary.RDFS_DOMAIN, terms);
					program.appendRule(factory.getCQIE(head));
					log.debug(head.toString());

					terms.clear();
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(predicate1.getName())));
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(description.toString())));
					head = factory.getFunction(OBDAVocabulary.RDFS_RANGE, terms);
					program.appendRule(factory.getCQIE(head));
					log.debug(head.toString());
					return true;

				}
				else // we assign the domain of the property and the range of
						// its inverse
				{

					terms.add(factory.getUriTemplate(factory.getConstantLiteral(predicate1.getName())));
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(description.toString())));
					Function head = factory.getFunction(OBDAVocabulary.RDFS_DOMAIN, terms);
					program.appendRule(factory.getCQIE(head));
					log.debug(head.toString());

					terms.clear();

					terms.add(factory.getUriTemplate(factory.getConstantLiteral(inverse.toString())));
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(description.toString())));
					head = factory.getFunction(OBDAVocabulary.RDFS_RANGE, terms);
					program.appendRule(factory.getCQIE(head));
					log.debug(head.toString());
					return true;
				}
			}
			else {
				return false;
			}
		}

		else {
			return false;

		}

	}

	/**
	 * Create a fact with the specified predicate as function. Use blank nodes for some property of description and URI for classes 
	 * 
	 * @param description1
	 * @param description2
	 * @param function
	 */

	private static void addBlankNodesRule(BasicClassDescription description1, BasicClassDescription description2, Predicate function) {

		Predicate predDescription1 = description1.getPredicate();
		Predicate predDescription2 = description2.getPredicate();
		List<Term> terms = new ArrayList<Term>();

		if (predDescription1.isClass())
		{
			// add URI terms
			terms.add(factory.getUriTemplate(factory.getConstantLiteral(description1.toString())));
		}
		else {
			if (!predDescription1.isDataTypePredicate()) { // propertySomeDescription
				// add blank node
				terms.add(factory.getConstantBNode(description1.toString()));
			}
		}

		if (predDescription2.isClass())
		{
			// add URI terms
			terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2.toString())));
		}
		else {
			if (!predDescription2.isDataTypePredicate()) { // propertySomeDescription
				// add blank node
				terms.add(factory.getConstantBNode(description2.toString()));
			}
		}

		if (terms.size() == 2) {
			Function head = factory.getFunction(function, terms);
			program.appendRule(factory.getCQIE(head));
			log.debug(head.toString());
		}

	}


	/**
	 * Create a fact given two nodes (transformed in URI) and a predicate function
	 * 
	 * @param description1
	 * @param description2
	 * @param function
	 */

	private static void addNodesRule(String description1, String description2, Predicate function) {

		List<Term> terms = new ArrayList<Term>();

		// add URI terms
		terms.add(factory.getUriTemplate(factory.getConstantLiteral(description1)));
		terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2)));

		Function head = factory.getFunction(function, terms);

		log.debug(head.toString());

		program.appendRule(factory.getCQIE(head));

	}

	


									
							

}
