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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class add facts to the datalog program regarding owl sparql entailments.
 * It is possible to activate or deactivate this feature in
 * Quest changing the preferences of QuestPreferences.SPARQL_OWL_ENTAILMENT
 * 
 */
public class OWLEntailmentsToFactRule {

	private static Logger log = LoggerFactory.getLogger(OWLEntailmentsToFactRule.class);

	private static OBDADataFactory factory = OBDADataFactoryImpl.getInstance();

	private static OntologyFactory ontoFactory = OntologyFactoryImpl.getInstance();

	private static DatalogProgram program;
	
	
	/**
	 * Add facts to the datalog program regarding owl sparql entailments.
	 * @param p the datalog program used later by the unfolder
	 * @param onto the ontology given by the user (starting .owl file)
	 *
	 */

	public static void addFacts(DatalogProgram p, Ontology onto) {

		program = p;

		TBoxReasoner reasoner = new TBoxReasonerImpl(onto);

		// add owl:equivalentClass, rdfs:subClassOf
		addEntailmentsForClasses(reasoner);
		
		// add owl:inverseOf, rdfs:subPropertyOf
		addEntailmentsForProperties(reasoner);
		
		// add owl:equivalentProperty, owl:disjointWith
		addDisjointClasses(onto.getDisjointBasicClassAxioms());
		
		// add owl:propertyDisjointWith
		addDisjointProperties(onto.getDisjointPropertyAxioms());
		
		//rdfs:range, rdfs:domain
		addRangesAndDomainsFromClasses(reasoner);

	}

	

	/**
	 * Add subclasses and equivalences in the database using the DAG. All subclasses are inserted
	 * considering also equivalences.
	 * 
	 * @param conn
	 * @param reasoner
	 * @throws SQLException
	 */

	private static void addEntailmentsForClasses(TBoxReasoner reasoner) {

		EquivalencesDAG<BasicClassDescription> dag = reasoner.getClasses();
		Iterator<Equivalences<BasicClassDescription>> it = dag.iterator();
		Predicate subClassOf = OBDAVocabulary.RDFS_SUBCLASS;

		while (it.hasNext()) {

			Equivalences<BasicClassDescription> eqv = it.next();

			Iterator<BasicClassDescription> iteq = eqv.getMembers().iterator();

			getEquivalencesClasses(eqv.getMembers());
			

			while (iteq.hasNext()) {

				BasicClassDescription classItem = iteq.next();

				// if we want to add all subrelations not only the direct one
				Iterator<Equivalences<BasicClassDescription>> classesIt = dag.getSub(eqv).iterator();

				while (classesIt.hasNext()) {

					Equivalences<BasicClassDescription> eqq = classesIt.next();
					Iterator<BasicClassDescription> itcl = eqq.getMembers().iterator();

					while (itcl.hasNext()) {

						BasicClassDescription subClassItem = itcl.next();

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
	 * @param equivalenceMaps
	 *            set of equivalent descriptions (classes, data-properties and
	 *            properties some description)
	 */

	private static void getEquivalencesClasses(Set<BasicClassDescription> equivalenceMaps) {

		Iterator<BasicClassDescription> itEquivalences = equivalenceMaps.iterator();
		Predicate equivalentClass = OBDAVocabulary.OWL_EQUIVALENT_CLASS;

		while (itEquivalences.hasNext()) {

			BasicClassDescription node1 = itEquivalences.next();
			Iterator<BasicClassDescription> itEquivalences2 = equivalenceMaps.iterator();

			while (itEquivalences2.hasNext()) {

				BasicClassDescription node2 = itEquivalences2.next();
				addBlankNodesRule(node1, node2, equivalentClass);

			}

		}

	}

	
	/**
	 * Add ranges and domains. 
	 * The domain is given by the direct named superclass, the range is given by the direct named subclass of a property some description.
	 * It iterates through the graph.
	 * It first search between the equivalent nodes if they are present, then it search for the direct superclass or subclass.
	 * @param reasoner
	 */
	
	private static void addRangesAndDomainsFromClasses(TBoxReasoner reasoner) {

		boolean foundRange;
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

						if (addRangeOrDomainRule(node1, node2, true))
							foundRange = true;
					}

				}

			}

			if (!foundRange) {

				Iterator<BasicClassDescription> iteq = eqv.getMembers().iterator();

				while (iteq.hasNext()) {

					BasicClassDescription classItem = iteq.next();

					if (classItem instanceof PropertySomeRestriction) {

						Iterator<Equivalences<BasicClassDescription>> classesIt = dag.getDirectSub(eqv).iterator();

						while (classesIt.hasNext()) {

							Equivalences<BasicClassDescription> eqq = classesIt.next();
							Iterator<BasicClassDescription> itcl = eqq.getMembers().iterator();

							while (itcl.hasNext()) {

								BasicClassDescription subClassItem = itcl.next();

								addRangeOrDomainRule(subClassItem, classItem, false);

							}

						}

						Iterator<Equivalences<BasicClassDescription>> classesIt2 = dag.getDirectSuper(eqv).iterator();

						while (classesIt2.hasNext()) {

							Equivalences<BasicClassDescription> eqq = classesIt2.next();
							Iterator<BasicClassDescription> itcl = eqq.getMembers().iterator();

							while (itcl.hasNext()) {

								BasicClassDescription superClassItem = itcl.next();

								addRangeOrDomainRule(classItem, superClassItem, false);

							}

						}
					}
				}
			}
		}

	}

	/**
	 * Create a fact given two blank nodes and a predicate
	 * 
	 * @param description1
	 * @param description2
	 * @param function
	 */

	private static void addBlankNodesRule(BasicClassDescription description1, BasicClassDescription description2, Predicate function) {

		List<Term> terms = new ArrayList<Term>();

		if (description1 instanceof OClass)
			// add URI terms
			terms.add(factory.getUriTemplate(factory.getConstantLiteral(description1.toString())));

		else if (description1 instanceof PropertySomeRestriction && !(((PropertySomeRestriction) description1).isInverse())) {
			// add blank node
			terms.add(factory.getConstantBNode(description1.toString()));
		}

		if (description2 instanceof OClass)
			// add URI terms
			terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2.toString())));

		else if (description2 instanceof PropertySomeRestriction && !(((PropertySomeRestriction) description2).isInverse())) {

			// add blank node
			terms.add(factory.getConstantBNode(description2.toString()));
		}

		if (terms.size() == 2) {
			Function head = factory.getFunction(function, terms);
			program.appendRule(factory.getCQIE(head));
			log.debug(head.toString());
		}

	}

	/**
	 * The domain of a property can be found looking at the subclass of property
	 * some description The range of a property can be found looking at the
	 * subclass of the inverse of property some description
	 * 
	 * @param subDescription
	 *            is analyzed if it is a property some restriction
	 * @param description
	 *            is analyzed if it is a class
	 */

	private static boolean addRangeOrDomainRule(BasicClassDescription subDescription, BasicClassDescription description, boolean equivalent) {

		List<Term> terms = new ArrayList<Term>();

		if (subDescription instanceof PropertySomeRestriction) {
			if (!(description instanceof PropertySomeRestriction)) {
				// get the property related to the existential
				Predicate property1 = ((PropertySomeRestriction) subDescription).getPredicate();

				if (((PropertySomeRestriction) subDescription).isInverse())
				{
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(property1.getName())));
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(description.toString())));
					Function head = factory.getFunction(OBDAVocabulary.RDFS_RANGE, terms);
					program.appendRule(factory.getCQIE(head));
					log.debug(head.toString());
					return true;

				}
				else
				{
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(property1.getName())));
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(description.toString())));
					Function head = factory.getFunction(OBDAVocabulary.RDFS_DOMAIN, terms);
					program.appendRule(factory.getCQIE(head));
					log.debug(head.toString());
					return true;
				}
			}
			else{
				return false;
			}
		}
		
		else {
			return false;

		}

	}


	/**
	 * Add subproperties and inverses in the database using the DAG. All subproperties are inserted
	 * considering also equivalences
	 * 
	 * @param conn
	 * @param reasoner
	 * @throws SQLException
	 */

	private static void addEntailmentsForProperties(TBoxReasoner reasoner) {

		EquivalencesDAG<Property> dag = reasoner.getProperties();
		Iterator<Equivalences<Property>> it = dag.iterator();
		Predicate subPropertyOf = OBDAVocabulary.RDFS_SUBPROPERTY;

		while (it.hasNext()) {

			Equivalences<Property> eqv = it.next();

			Iterator<Property> iteq = eqv.getMembers().iterator();
			
			//add owl:inversesOf

			addInversesAndEquivalences(eqv.getMembers());

			while (iteq.hasNext()) {

				Property propertyItem = iteq.next();

				// if we want to add all subrelations not only the direct one
				Iterator<Equivalences<Property>> propertiesIt = dag.getSub(eqv).iterator();

				while (propertiesIt.hasNext()) {

					Equivalences<Property> eqq = propertiesIt.next();
					Iterator<Property> itcl = eqq.getMembers().iterator();

					while (itcl.hasNext()) {

						Property subPropertyItem = itcl.next();

						// added not to consider sub equal to the current same
						// node
						if ((!propertyItem.isInverse()) && (!subPropertyItem.isInverse())) {

							addNodesRule(subPropertyItem.toString(), propertyItem.toString(), subPropertyOf);

						}

					}
				}
			}

		}

	}

	
	/**
	 * Called by { @link #addEntailmentsForProperties(TBoxReasoner) }
	 * Add inverses and equivalences of properties
	 * @param members
	 */
	private static void addInversesAndEquivalences(Set<Property> members) {

		Predicate inverseOf = OBDAVocabulary.OWL_INVERSE;
		Predicate equivalent= OBDAVocabulary.OWL_EQUIVALENT_PROPERTY;
		
		Iterator<Property> properties = members.iterator();

		while (properties.hasNext()) {
			Property propertyItem = properties.next();

			if (!propertyItem.isInverse()) {
				Iterator<Property> properties2 = members.iterator();

				while (properties2.hasNext()) {
					Property propertyItem2 = properties2.next();

					if(!propertyItem2.isInverse()) {
						addNodesRule(propertyItem.toString(), propertyItem2.toString(), equivalent);
					}
					if (!propertyItem.getPredicate().equals(propertyItem2.getPredicate()) && propertyItem2.isInverse()) {
						Property inverse = ontoFactory.createProperty(propertyItem2.getPredicate(), false);
						addNodesRule(propertyItem.toString(), inverse.toString(), inverseOf);
					}
				}
			}

		}

	}

	/**
	 * Create a fact given two nodes (transformed in URI) and a predicate
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

	/**
	 * From the given ontology add the facts for owl:disjointWith
	 * 
	 * @param set
	 *            set of disjoint axiom with class and property some description
	 */

	private static void addDisjointClasses(Set<DisjointBasicClassAxiom> set) {

		Predicate disjointClass = OBDAVocabulary.OWL_DISJOINT_CLASS;

		for (DisjointBasicClassAxiom disjointElements : set) {

			for (Predicate description1 : disjointElements.getReferencedEntities()) {

				for (Predicate description2 : disjointElements.getReferencedEntities()) {

					if (!description1.equals(description2)) {

						List<Term> terms = new ArrayList<Term>();

						if (description1.isClass())
							// add URI terms
							terms.add(factory.getUriTemplate(factory.getConstantLiteral(description1.getName())));

						else { // if property some description
								// add blank node
							terms.add(factory.getConstantBNode(description1.getName()));
						}

						if (description2.isClass())
							// add URI terms
							terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2.getName())));

						else { // if property some description

							// add blank node
							terms.add(factory.getConstantBNode(description2.getName()));
						}

						if (terms.size() == 2) {
							Function head = factory.getFunction(disjointClass, terms);
							program.appendRule(factory.getCQIE(head));
						}
					}

				}
			}

		}
	}
	
	private static void addDisjointProperties(Set<DisjointPropertyAxiom> set) {
		
		Predicate disjointProperty = OBDAVocabulary.OWL_DISJOINT_PROPERTY;

		for (DisjointPropertyAxiom disjointElements : set) {

			for (Predicate description1 : disjointElements.getReferencedEntities()) {

				for (Predicate description2 : disjointElements.getReferencedEntities()) {

					if (!description1.equals(description2)) {

						addNodesRule(description1.getName(), description2.getName(), disjointProperty);
					}

				}
			}

		}
	}

}
