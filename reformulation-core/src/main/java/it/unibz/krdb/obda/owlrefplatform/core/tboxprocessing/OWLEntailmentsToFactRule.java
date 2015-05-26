package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class add facts to the datalog program regarding owl sparql entailments.
 * It is possible to activate or deactivate this feature in Quest changing the
 * preferences of QuestPreferences.SPARQL_OWL_ENTAILMENT
 * 
 */



public class OWLEntailmentsToFactRule {

	private static Logger log = LoggerFactory.getLogger(OWLEntailmentsToFactRule.class);

	private static OBDADataFactory factory = OBDADataFactoryImpl.getInstance();

	private static OntologyFactory ontoFactory = OntologyFactoryImpl.getInstance();

	private static List<CQIE> program;
	
	private static TBoxReasoner reasoner;

	private static EquivalencesDAG<ClassExpression> classDAG;

	private static EquivalencesDAG<ObjectPropertyExpression> objectPropertyDAG;

	private static EquivalencesDAG<DataPropertyExpression> dataPropertyDAG;

	private static EquivalencesDAG<DataRangeExpression> dataRangeDAG;

	/**
	 * Add facts to the datalog program regarding owl sparql entailments.
	 * 
	 * @param p
	 *            the datalog program used later by the unfolder
	 * @param onto
	 *            the ontology given by the user (starting .owl file)
	 *
	 * 
	 */

	public static List<CQIE> addFacts(List<CQIE> p, Ontology onto) {

		program = p;

		reasoner = new TBoxReasonerImpl(onto);

		classDAG = reasoner.getClassDAG();

		objectPropertyDAG = reasoner.getObjectPropertyDAG();

		dataPropertyDAG = reasoner.getDataPropertyDAG();

		dataRangeDAG =  reasoner.getDataRangeDAG();


		log.info("addEntailmentsClass");
		// add owl:equivalentClass, rdfs:subClassOf
		addEntailmentsForClasses();

		log.info("addEntailmentsProperty");
		// add owl:inverseOf, owl:equivalentProperty, rdfs:subPropertyOf
		addEntailmentsForDataProperties();

		addEntailmentsForObjectProperties();

		log.info("addDisjointWith");
		// add owl:disjointWith
		addDisjointClasses(onto.getDisjointClassesAxioms());

		log.info("propertyDisjointWith");
		// add owl:propertyDisjointWith
		addDisjointObjectProperties(onto.getDisjointObjectPropertiesAxioms());
		addDisjointDataProperties(onto.getDisjointDataPropertiesAxioms());

		log.info("domain and range");
		// rdfs:range, rdfs:domain
		addObjectRangesAndDomainsFromClasses();
		addDataRangesAndDomainsFromClasses();


		return program;

	}





	/**
	 * Add subclasses and equivalences in the datalog program using the DAG with classes. 
	 * 
	 */

	private static void addEntailmentsForClasses() {



		Iterator<Equivalences<ClassExpression>> itClass = classDAG.iterator();

		Predicate subClassOf = OBDAVocabulary.RDFS_SUBCLASS;

		while (itClass.hasNext()) {

			Equivalences<ClassExpression> eqv = itClass.next();

			Iterator<ClassExpression> iteq = eqv.getMembers().iterator();

			while (iteq.hasNext()) {

				ClassExpression classItem = iteq.next();

				/*
				 * Add the equivalences owl:equivalentClass for each node in the
				 * graph
				 */
				getEquivalencesClasses(classItem, eqv.getMembers());

				// iterate through all subclasses not only the direct ones
				Iterator<Equivalences<ClassExpression>> classesIt = classDAG.getSub(eqv).iterator();

				while (classesIt.hasNext()) {

					Equivalences<ClassExpression> eqq = classesIt.next();
					Iterator<ClassExpression> itcl = eqq.getMembers().iterator();

					while (itcl.hasNext()) {

						ClassExpression subClassItem = itcl.next();

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

	private static void getEquivalencesClasses(ClassExpression description, Set<ClassExpression> equivalentMembers) {

		Iterator<ClassExpression> itEquivalences = equivalentMembers.iterator();
		Predicate equivalentClass = OBDAVocabulary.OWL_EQUIVALENT_CLASS;

		while (itEquivalences.hasNext()) {

			ClassExpression node2 = itEquivalences.next();

			/*
			 * Add to datalog program equivalences owl:equivalentClass create a
			 * blank node if some property of description
			 */
			addBlankNodesRule(description, node2, equivalentClass);

		}

	}

	/**
	 * Add subproperties, equivalences and inverses in the datalog program using the DAG with data properties.
	 *
	 */

	private static void addEntailmentsForDataProperties() {


		Iterator<Equivalences<DataPropertyExpression>> itProperty = dataPropertyDAG.iterator();

		Predicate subPropertyOf = OBDAVocabulary.RDFS_SUBPROPERTY;

		while (itProperty.hasNext()) {

			Equivalences<DataPropertyExpression> eqv = itProperty.next();

			Iterator<DataPropertyExpression> iteq = eqv.getMembers().iterator();

			while (iteq.hasNext()) {

				DataPropertyExpression propertyItem = iteq.next();

				// add to datalog program owl:inversesOf and
				// owl:equivalentProperty

				getDataEquivalences(propertyItem, eqv.getMembers());

				// add all subproperty not only the direct ones
				Iterator<Equivalences<DataPropertyExpression>> propertiesIt = dataPropertyDAG.getSub(eqv).iterator();

				while (propertiesIt.hasNext()) {

					Equivalences<DataPropertyExpression> eqq = propertiesIt.next();
					Iterator<DataPropertyExpression> itcl = eqq.getMembers().iterator();

					while (itcl.hasNext()) {

						DataPropertyExpression subPropertyItem = itcl.next();

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
	 * Add subproperties, equivalences and inverses in the datalog program using the DAG with data properties.
	 *
	 */

	private static void addEntailmentsForObjectProperties() {


		Iterator<Equivalences<ObjectPropertyExpression>> itProperty = objectPropertyDAG.iterator();

		Predicate subPropertyOf = OBDAVocabulary.RDFS_SUBPROPERTY;

		while (itProperty.hasNext()) {

			Equivalences<ObjectPropertyExpression> eqv = itProperty.next();

			Iterator<ObjectPropertyExpression> iteq = eqv.getMembers().iterator();

			while (iteq.hasNext()) {

				ObjectPropertyExpression propertyItem = iteq.next();

				// add to datalog program owl:inversesOf and
				// owl:equivalentProperty

				addInversesAndEquivalences(propertyItem, eqv.getMembers());

				// add all subproperty not only the direct ones
				Iterator<Equivalences<ObjectPropertyExpression>> propertiesIt = objectPropertyDAG.getSub(eqv).iterator();

				while (propertiesIt.hasNext()) {

					Equivalences<ObjectPropertyExpression> eqq = propertiesIt.next();
					Iterator<ObjectPropertyExpression> itcl = eqq.getMembers().iterator();

					while (itcl.hasNext()) {

						ObjectPropertyExpression subPropertyItem = itcl.next();

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
	 * Called by { @link #addEntailmentsForObjectProperties(TBoxReasoner) }
	 * Add inverses and equivalences of  object properties to datalog program
	 * 
	 * @param equivalentMembers
	 */
	private static void addInversesAndEquivalences(ObjectPropertyExpression property1, Set<ObjectPropertyExpression> equivalentMembers) {

		Predicate inverseOf = OBDAVocabulary.OWL_INVERSE;
		Predicate equivalent = OBDAVocabulary.OWL_EQUIVALENT_PROPERTY;

		Iterator<ObjectPropertyExpression> properties = equivalentMembers.iterator();

		while (properties.hasNext()) {

			ObjectPropertyExpression propertyItem2 = properties.next();

			/*
			 * Add owl:equivalentProperty to the datalog program
			 */
			addNodesRule(property1.toString(), propertyItem2.toString(), equivalent);

			ObjectPropertyExpression inverse;

			// add the inverse of the equivalent node
				inverse = propertyItem2.getInverse();

			/*
			 * Add owl:inverseOf to the datalog program
			 */
			addNodesRule(property1.toString(), inverse.toString(), inverseOf);
		}

	}

	private static void getDataEquivalences(DataPropertyExpression property1, Set<DataPropertyExpression> equivalentMembers) {

		Predicate equivalent = OBDAVocabulary.OWL_EQUIVALENT_PROPERTY;

		Iterator<DataPropertyExpression> properties = equivalentMembers.iterator();

		while (properties.hasNext()) {

			DataPropertyExpression propertyItem2 = properties.next();

			/*
			 * Add owl:equivalentProperty to the datalog program
			 */
			addNodesRule(property1.toString(), propertyItem2.toString(), equivalent);


		}

	}


	/**
	 * From the given ontology and the DAG with classes, add the facts for owl:disjointWith to datalog program
	 *
	 * @param setDisjointClasses
	 *            set of disjoint axiom with predicates of class and property some description. It does not contain inverses
	 */

	private static void addDisjointClasses(List<NaryAxiom<ClassExpression>> setDisjointClasses) {

		Predicate disjointClass = OBDAVocabulary.OWL_DISJOINT_CLASS;



		for (NaryAxiom<ClassExpression> disjointElements : setDisjointClasses) {

				for(ClassExpression disjointElement1 :disjointElements.getComponents()) {

					Equivalences<ClassExpression> equivalentDescriptions = classDAG.getVertex(disjointElement1);

					// add disjoints considering equivalent nodes and subclasses
					Set<Equivalences<ClassExpression>> classes = classDAG.getSub(equivalentDescriptions);

					for (Equivalences<ClassExpression> equivalentclass : classes) {

						for (ClassExpression description1 : equivalentclass.getMembers()) {

							for (ClassExpression disjointElement2 : disjointElements.getComponents()) {


								// a class cannot be disjoint with itself or one of
								// its equivalent classes
								if (!disjointElement1.equals(disjointElement2)) {


									Equivalences<ClassExpression> equivalentDescriptions2 = classDAG.getVertex(disjointElement2);

									Set<Equivalences<ClassExpression>> classes2 = classDAG.getSub(equivalentDescriptions2);

									for (Equivalences<ClassExpression> equivalentclasses2 : classes2) {

										for (ClassExpression description2 : equivalentclasses2.getMembers()) {

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
	 * @param disjointDataPropertiesAxioms
	 *            set of disjoint axiom with predicates of properties. It does not contain inverses.
	 */

	private static void addDisjointDataProperties(List<NaryAxiom<DataPropertyExpression>> disjointDataPropertiesAxioms) {

		Predicate disjointProperty = OBDAVocabulary.OWL_DISJOINT_PROPERTY;

		for (NaryAxiom<DataPropertyExpression> disjointElements : disjointDataPropertiesAxioms) {

			for(DataPropertyExpression disjointElement1 :disjointElements.getComponents()) {

				Equivalences<DataPropertyExpression> equivalentDescriptions = dataPropertyDAG.getVertex(disjointElement1);

				// add disjoints considering equivalent nodes and subclasses
				Set<Equivalences<DataPropertyExpression>> properties = dataPropertyDAG.getSub(equivalentDescriptions);

				for (Equivalences<DataPropertyExpression> dataPropertyExpressions : properties) {

					for (DataPropertyExpression description1 : dataPropertyExpressions.getMembers()) {

						for (DataPropertyExpression disjointElement2 : disjointElements.getComponents()) {


							// a class cannot be disjoint with itself or one of
							// its equivalent classes
							if (!disjointElement1.equals(disjointElement2)) {


								Equivalences<DataPropertyExpression> equivalentDescriptions2 = dataPropertyDAG.getVertex(disjointElement2);

								Set<Equivalences<DataPropertyExpression>> properties2 = dataPropertyDAG.getSub(equivalentDescriptions2);

								for (Equivalences<DataPropertyExpression> objectPropertyExpressions2 : properties2) {

									for (DataPropertyExpression description2 : objectPropertyExpressions2.getMembers()) {

										/*
									 * Add owl:propertyDisjointWith facts to the
									 * datalog program
									 */
										addNodesRule(description1.toString(), description2.toString(), disjointProperty);




									}

								}

							}


						}
					}
				}
			}
		}
	}

	private static void addDisjointObjectProperties(List<NaryAxiom<ObjectPropertyExpression>> disjointObjectPropertiesAxioms) {

		Predicate disjointProperty = OBDAVocabulary.OWL_DISJOINT_PROPERTY;

		for (NaryAxiom<ObjectPropertyExpression> disjointElements : disjointObjectPropertiesAxioms) {

			for(ObjectPropertyExpression disjointElement1 :disjointElements.getComponents()) {

				Equivalences<ObjectPropertyExpression> equivalentDescriptions = objectPropertyDAG.getVertex(disjointElement1);

				// add disjoints considering equivalent nodes and subclasses
				Set<Equivalences<ObjectPropertyExpression>> properties = objectPropertyDAG.getSub(equivalentDescriptions);

				for (Equivalences<ObjectPropertyExpression> objectPropertyExpressions : properties) {

					for (ObjectPropertyExpression description1 : objectPropertyExpressions.getMembers()) {

						for (ObjectPropertyExpression disjointElement2 : disjointElements.getComponents()) {


							// a class cannot be disjoint with itself or one of
							// its equivalent classes
							if (!disjointElement1.equals(disjointElement2)) {


								Equivalences<ObjectPropertyExpression> equivalentDescriptions2 = objectPropertyDAG.getVertex(disjointElement2);

								Set<Equivalences<ObjectPropertyExpression>> properties2 = objectPropertyDAG.getSub(equivalentDescriptions2);

								for (Equivalences<ObjectPropertyExpression> objectPropertyExpressions2 : properties2) {

									for (ObjectPropertyExpression description2 : objectPropertyExpressions2.getMembers()) {

										/*
									 * Add owl:propertyDisjointWith facts to the
									 * datalog program
									 */
										addNodesRule(description1.toString(), description2.toString(), disjointProperty);

										/*
									 * Add owl:propertyDisjointWith facts for
									 * the inverses to the datalog program
									 */

										addNodesRule(description1.getInverse().toString(), description2.getInverse().toString(), disjointProperty);
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
	 * Add rdfs:domain and rdfs:range. The domain of a property P is given by the direct named (OClass)
	 * superclass C of the property some description existsP, the range of a property is given by the domain of the inverse
	 * The method iterates through the DAG with classes. It first searches between
	 * the equivalent nodes (if they are present)  and then it searches in the
	 * direct superclasses or subclasses.
	 * 
	 */


	private static void addObjectRangesAndDomainsFromClasses() {

		boolean foundRange; // if true we found the range or domain between the
		// equivalent classes


		Iterator<Equivalences<ObjectPropertyExpression>> it = objectPropertyDAG.iterator();

		while (it.hasNext()) {

			foundRange = false;

			Equivalences<ObjectPropertyExpression> eqv = it.next();

			Iterator<ObjectPropertyExpression> itEquivalences = eqv.getMembers().iterator();

			while (itEquivalences.hasNext()) {

				ObjectPropertyExpression node1 = itEquivalences.next();

				Equivalences<ClassExpression> domainEquiv= classDAG.getVertex(node1.getDomain());

//				System.out.println(domainEquiv.getRepresentative());

				Iterator<ClassExpression> iteq = domainEquiv.getMembers().iterator();

				while (iteq.hasNext()) {
					ClassExpression classItem = iteq.next();
					if (classItem instanceof OClass){

						addNodesRule(node1.toString(), classItem.toString(), OBDAVocabulary.RDFS_DOMAIN );
						foundRange = true;

					}

				}

				/*
			 * Search between superclasses the domain and range if there were no
			 * named classes in the equivalent nodes.
			 */
				if (!foundRange) {

					Iterator<Equivalences<ClassExpression>> classesIt2 = classDAG.getSuper(domainEquiv).iterator();

					while (classesIt2.hasNext()) {

						Equivalences<ClassExpression> eqq = classesIt2.next();
						Iterator<ClassExpression> itcl = eqq.getMembers().iterator();

						while (itcl.hasNext()) {
							ClassExpression classItem = itcl.next();
							if (classItem instanceof OClass) {

								addNodesRule(node1.toString(), classItem.toString(), OBDAVocabulary.RDFS_DOMAIN);
								foundRange = true;


							}
						}

					}

				}

				foundRange = false;

				Equivalences<ClassExpression> rangeEquiv= classDAG.getVertex(node1.getRange());

//				System.out.println(rangeEquiv.getRepresentative());

				Iterator<ClassExpression> iteq2 = rangeEquiv.getMembers().iterator();

				while (iteq2.hasNext()) {
					ClassExpression classItem = iteq2.next();
					if (classItem instanceof OClass){

						addNodesRule(node1.toString(), classItem.toString(), OBDAVocabulary.RDFS_RANGE );
						foundRange = true;

					}

				}

				if (!foundRange) {

					Iterator<Equivalences<ClassExpression>> classesIt2 = classDAG.getDirectSuper(domainEquiv).iterator();

					while (classesIt2.hasNext()) {

						Equivalences<ClassExpression> eqq = classesIt2.next();
						Iterator<ClassExpression> itcl = eqq.getMembers().iterator();

						while (itcl.hasNext()) {
							ClassExpression classItem = itcl.next();

							if (classItem instanceof OClass) {

								addNodesRule(node1.toString(), classItem.toString(), OBDAVocabulary.RDFS_RANGE);



							}
						}

					}

				}

				foundRange = false;





			}

		}

	}


	private static void addDataRangesAndDomainsFromClasses() {

		boolean foundRange; // if true we found the range or domain between the
		// equivalent classes


		Iterator<Equivalences<DataPropertyExpression>> it = dataPropertyDAG.iterator();

		while (it.hasNext()) {

			foundRange = false;

			Equivalences<DataPropertyExpression> eqv = it.next();

			Iterator<DataPropertyExpression> itEquivalences = eqv.getMembers().iterator();

			while (itEquivalences.hasNext()) {

				DataPropertyExpression node1 = itEquivalences.next();

				Equivalences<ClassExpression> domainEquiv= classDAG.getVertex(node1.getDomain());

//				System.out.println(domainEquiv.getRepresentative());

				Iterator<ClassExpression> iteq = domainEquiv.getMembers().iterator();

				while (iteq.hasNext()) {
					ClassExpression classItem = iteq.next();
					if (classItem instanceof OClass){

						addNodesRule(node1.toString(), classItem.toString(), OBDAVocabulary.RDFS_DOMAIN );
						foundRange = true;

					}

				}

				/*
			 * Search between superclasses the domain and range if there were no
			 * named classes in the equivalent nodes.
			 */
				if (!foundRange) {

					Iterator<Equivalences<ClassExpression>> classesIt2 = classDAG.getSuper(domainEquiv).iterator();

					while (classesIt2.hasNext()) {

						Equivalences<ClassExpression> eqq = classesIt2.next();
						Iterator<ClassExpression> itcl = eqq.getMembers().iterator();

						while (itcl.hasNext()) {
							ClassExpression classItem = itcl.next();
							if (classItem instanceof OClass) {

								addNodesRule(node1.toString(), classItem.toString(), OBDAVocabulary.RDFS_DOMAIN);


							}
						}

					}

				}

				foundRange = false;

				DataPropertyRangeExpression range = node1.getRange();
				Equivalences<DataRangeExpression> rangeEquiv= dataRangeDAG.getVertex(range);

				System.out.println(rangeEquiv.getRepresentative());

				Iterator<DataRangeExpression> iteq2 = rangeEquiv.getMembers().iterator();

				while (iteq2.hasNext()) {
					DataRangeExpression datatypeItem = iteq2.next();
					if (datatypeItem instanceof Datatype){

						addNodesRule(node1.toString(), datatypeItem.toString(), OBDAVocabulary.RDFS_RANGE );
						foundRange = true;

					}

				}

				if (!foundRange) {

					Iterator<Equivalences<DataRangeExpression>> datatypesIt2 = dataRangeDAG.getDirectSuper(rangeEquiv).iterator();

					while (datatypesIt2.hasNext()) {

						Equivalences<DataRangeExpression> eqq = datatypesIt2.next();
						Iterator<DataRangeExpression> itcl = eqq.getMembers().iterator();

						while (itcl.hasNext()) {
							DataRangeExpression datatypeItem = itcl.next();

							if (datatypeItem instanceof Datatype) {

								addNodesRule(node1.toString(), datatypeItem.toString(), OBDAVocabulary.RDFS_RANGE);



							}
						}

					}

				}

				foundRange = false;





			}

		}

	}



	/**
	 * Create a fact with the specified predicate as function. Use blank nodes for some property of description and URI for classes 
	 * 
	 * @param description1
	 * @param description2
	 * @param function
	 */

	private static void addBlankNodesRule(ClassExpression description1, ClassExpression description2, Predicate function) {


		List<Term> terms = new ArrayList<Term>();

		if (description1 instanceof OClass)
		{
			// add URI terms
			terms.add(factory.getUriTemplate(factory.getConstantLiteral(description1.toString())));
		}
//		else {
//			if (description1 instanceof ObjectSomeValuesFrom || description1 instanceof DataSomeValuesFrom) {
//			 // propertySomeDescription
//				// add blank node
//				terms.add(factory.getConstantBNode(description1.toString()));
//			}


//
//		}

		if (description2 instanceof OClass)
		{
			// add URI terms
			terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2.toString())));
		}
//		else {
//			if  (description2 instanceof ObjectSomeValuesFrom || description2 instanceof DataSomeValuesFrom) {
//				// propertySomeDescription
//				// add blank node
//				terms.add(factory.getConstantBNode(description2.toString()));
//			}
//
//		}

		if (terms.size() == 2) {
			Function head = factory.getFunction(function, terms);
			program.add(factory.getCQIE(head));
//			log.debug(head.toString());
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

//		log.debug(head.toString());

		program.add(factory.getCQIE(head));

	}

	


									
							

}
