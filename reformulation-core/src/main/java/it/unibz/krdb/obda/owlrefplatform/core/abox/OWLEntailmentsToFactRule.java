package it.unibz.krdb.obda.owlrefplatform.core.abox;

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
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
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
 * This class add facts to the datalog program regarding rdfs:subClassOf and
 * rdfs:subPropertyOf It is possible to activate or deactivate this feature in
 * Quest changing the preferences of QuestPreferences.SPARQL_OWL_ENTAILMENT
 * 
 */
public class OWLEntailmentsToFactRule {

	private static Logger log = LoggerFactory.getLogger(OWLEntailmentsToFactRule.class);

	private static OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
	
	private static OntologyFactory ontoFactory = OntologyFactoryImpl.getInstance(); 

	private static DatalogProgram program;
	
	private static Map<Predicate, Description> equivalences;

	public static void addFacts(DatalogProgram p, Ontology onto, Map<Predicate, Description> equivalenceMaps) {

		program = p;
		equivalences = equivalenceMaps;
		
		TBoxReasoner reasoner = new TBoxReasonerImpl(onto);
		
		//add owl:equivalentClass, rdfs:range, rdfs:domain, rdfs:subClassOf
		addEntailmentsForClasses(reasoner);
		//add rdfs:subPropertyOf
		addSubRolesFromOntology(reasoner);
		//add owl:disjointWith
		addDisjointClasses(onto.getDisjointBasicClassAxioms());
		


		
		 
	}
	
	/**
	 * Add subclasses in the database using the DAG. All subclasses are inserted
	 * considering also equivalences.
	 * Add equivalences, rdfs:range and rdfs:domain 
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
			
			getEquivalences(eqv.getMembers());
//			getEquivalences(dag.getVertex(eqv.getRepresentative()).getMembers());

			while (iteq.hasNext()) {

				BasicClassDescription classItem = iteq.next();

				// if we want to add all subrelations not only the direct one
				Iterator<Equivalences<BasicClassDescription>> classesIt = dag.getSub(eqv).iterator();

				while (classesIt.hasNext()) {

					Equivalences<BasicClassDescription> eqq = classesIt.next();
					Iterator<BasicClassDescription> itcl = eqq.getMembers()
							.iterator();

					while (itcl.hasNext()) {

						BasicClassDescription subClassItem = itcl.next();

						addBlankNodesRule(subClassItem, classItem, subClassOf);
						

						log.debug("Insert class: " + classItem);
						log.debug("SubClass member: " + subClassItem);

					}

				}
			}
		}
		
		addRolesAndRangesFromClasses(reasoner);
		

	}

	/**
	 * Called by { @link #addEntailmentsForClasses(TBoxReasoner) }  add the owl:equivalentClass and 
	 * rdfs:range/ rdfs:domain from the equivalenceMaps 
	 * @param equivalenceMaps set of equivalent descriptions (classes, data-properties and properties some description)
	 */
	
	private static void getEquivalences(Set<BasicClassDescription> equivalenceMaps) {
		
		Iterator<BasicClassDescription> itEquivalences = equivalenceMaps.iterator();
		Predicate equivalentClass = OBDAVocabulary.OWL_EQUIVALENT;
		
		while (itEquivalences.hasNext()) {

			BasicClassDescription node1 = itEquivalences.next();
			Iterator<BasicClassDescription> itEquivalences2 = equivalenceMaps.iterator();
			
			while (itEquivalences2.hasNext()) {

				BasicClassDescription node2 = itEquivalences2.next();
				addBlankNodesRule(node1, node2, equivalentClass);
				addRangeOrDomainRule(node1, node2);
			}
		
		}

	}

	
	private static void addRolesAndRangesFromClasses(TBoxReasoner reasoner) {

		EquivalencesDAG<BasicClassDescription> dag = reasoner.getClasses();
		Iterator<Equivalences<BasicClassDescription>> it = dag.iterator();
		
		while (it.hasNext()) {

			Equivalences<BasicClassDescription> eqv = it.next();

			Iterator<BasicClassDescription> iteq = eqv.getMembers().iterator();
			

			if (eqv.size()==1) {

				BasicClassDescription classItem = iteq.next();

				
				// if we want to add all subrelations not only the direct one
				Iterator<Equivalences<BasicClassDescription>> classesIt = dag.getDirectSub(eqv).iterator();

				while (classesIt.hasNext()) {

					Equivalences<BasicClassDescription> eqq = classesIt.next();
					Iterator<BasicClassDescription> itcl = eqq.getMembers()
							.iterator();

					while (itcl.hasNext()) {

						BasicClassDescription subClassItem = itcl.next();

						addRangeOrDomainRule(subClassItem, classItem);
						
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
			terms.add( factory.getConstantBNode(description2.toString()));
		}

		if (terms.size() == 2) {
			Function head = factory.getFunction(function, terms);
			program.appendRule(factory.getCQIE(head));
		}

	}
	
	
	/**
	 * The domain of a property can be found looking at the subclass of property some description
	 *  The range of a property can be found looking at the subclass of the inverse of property some description
	 * @param subDescription is analyzed if it is a class
	 * @param description is analyzed if it is a property some restriction
	 */
	
	private static void addRangeOrDomainRule(BasicClassDescription subDescription, BasicClassDescription description) {

		List<Term> terms = new ArrayList<Term>();

		if (subDescription instanceof PropertySomeRestriction) {
			if (!(description instanceof PropertySomeRestriction ))  {
				// get the property related to the existential
				Predicate property1 = ((PropertySomeRestriction) subDescription).getPredicate();

				if (((PropertySomeRestriction) subDescription).isInverse())
				{
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(property1.getName())));
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(description.toString())));
					Function head = factory.getFunction(OBDAVocabulary.RDFS_RANGE, terms);
					program.appendRule(factory.getCQIE(head));

				}
				else
				{
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(property1.getName())));
					terms.add(factory.getUriTemplate(factory.getConstantLiteral(description.toString())));
					Function head = factory.getFunction(OBDAVocabulary.RDFS_DOMAIN, terms);
					program.appendRule(factory.getCQIE(head));
				}
			}
		}

	}

	/**
	 * Add subroles in the database using the DAG. All subroles are inserted
	 * considering also equivalences
	 * 
	 * @param conn
	 * @param reasoner
	 * @throws SQLException
	 */

	private static void addSubRolesFromOntology(TBoxReasoner reasoner) {

		EquivalencesDAG<Property> dag = reasoner.getProperties();
		Iterator<Equivalences<Property>> it = dag.iterator();
		Predicate subPropertyOf = OBDAVocabulary.RDFS_SUBPROPERTY;

		while (it.hasNext()) {

			Equivalences<Property> eqv = it.next();

			Iterator<Property> iteq = eqv.getMembers().iterator();
			
			addInverses(eqv.getMembers());

			while (iteq.hasNext()) {

				Property propertyItem = iteq.next();
				
				
				// log.debug("New property member: " + propertyItem);

				// if we want to add all subrelations not only the direct one
				Iterator<Equivalences<Property>> propertiesIt = dag.getSub(eqv).iterator();

				while (propertiesIt.hasNext()) {

					Equivalences<Property> eqq = propertiesIt.next();
					Iterator<Property> itcl = eqq.getMembers()
							.iterator();

					while (itcl.hasNext()) {

						Property subPropertyItem = itcl.next();

						// added not to consider sub equal to the current same
						// node
						if ((!propertyItem.isInverse()) && (!subPropertyItem.isInverse())) {
							log.debug("Insert property: " + propertyItem);
							log.debug("SubProperty member: " + subPropertyItem);

							addNodesRule(subPropertyItem.toString(), propertyItem.toString(), subPropertyOf);

						}

					}
				}
			}

		}

	}
	
	
	private static void addInverses(Set<Property> members) {
		
		Predicate inverseOf = OBDAVocabulary.OWL_INVERSE;
		Iterator<Property> properties = members.iterator();
		
		while (properties.hasNext()) {
			Property propertyItem = properties.next();
			
			if (!propertyItem.isInverse()) {
				Iterator<Property> properties2 = members.iterator();
				
				while (properties2.hasNext()) {
					Property propertyItem2 = properties2.next();
				
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

		log.debug("head: " + head);

		program.appendRule(factory.getCQIE(head));

	}


	
	/**
	 * From the given ontology add the facts for owl:disjointWith 
	 * @param set set of disjoint axiom with class and property some description
	 */

	private static void addDisjointClasses(Set<DisjointBasicClassAxiom> set) {

		Predicate disjointClass = OBDAVocabulary.OWL_DISJOINT;
		
		for (DisjointBasicClassAxiom disjointElements : set) {

			for (Predicate description1 : disjointElements.getReferencedEntities()) {

				for (Predicate description2 : disjointElements.getReferencedEntities()) {

					if (!description1.equals(description2)) {
						
						
						List<Term> terms = new ArrayList<Term>();

						if (description1.isClass())
							// add URI terms
							terms.add(factory.getUriTemplate(factory.getConstantLiteral(description1.toString())));

						else { // if property some description
							// add blank node
							terms.add(factory.getConstantBNode(description1.toString()));
						}

						if (description2.isClass())
							// add URI terms
							terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2.toString())));

						else { //if property some description

							// add blank node
							terms.add( factory.getConstantBNode(description2.toString()));
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
	
	
	
	

}
