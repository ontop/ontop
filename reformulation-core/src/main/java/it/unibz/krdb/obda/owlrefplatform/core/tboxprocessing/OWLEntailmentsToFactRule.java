package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyVocabularyImpl;
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

		log.info("addpropertyDisjointWith");
		// add owl:propertyDisjointWith
		addDisjointObjectProperties(onto.getDisjointObjectPropertiesAxioms());
		addDisjointDataProperties(onto.getDisjointDataPropertiesAxioms());
		


		return program;

	}


	/**
	 * Add subclasses and equivalences in the datalog program using the DAG with classes. 
	 * 
	 */

	private static void addEntailmentsForClasses() {

		Iterator<Equivalences<ClassExpression>> itClass = classDAG.iterator();


		while (itClass.hasNext()) {

			Equivalences<ClassExpression> eqv = itClass.next();

			Set<ClassExpression> equivalentClasses = eqv.getMembers();
			Iterator<ClassExpression> iteq = equivalentClasses.iterator();

			while (iteq.hasNext()) {

				ClassExpression classItem = iteq.next();

				/*
				 * Add the equivalences owl:equivalentClass for each node in the
				 * graph
				 */
//				log.info ("Add equivalent classes");
				addEquivalentClasses(classItem, equivalentClasses);
//				log.info ("Add subClasses");
				addSubClasses(classItem, classDAG.getSub(eqv));


			}
		}

	}
	

	/**
	 * Called by { @link #addEntailmentsForClasses() } add the
	 * owl:equivalentClass 
	 * 
	 * @param equivalentClasses
	 *            set of equivalent classes
	 */

	private static void addEquivalentClasses(ClassExpression description, Set<ClassExpression> equivalentClasses) {


		Iterator<ClassExpression> itEquivalences = equivalentClasses.iterator();
		Predicate equivalentClass = OBDAVocabulary.OWL_EQUIVALENT_CLASS;

		while (itEquivalences.hasNext()) {

			ClassExpression node2 = itEquivalences.next();

			/*
			 * Add to datalog program equivalences owl:equivalentClass
			 */
//			addBlankNodesRule(description, node2, equivalentClass);
			addClasses(description, node2, equivalentClass);

		}

	}

	/**
	 * Called by { @link #addEntailmentsForClasses() } add the
	 * rdfs:subclass
	 *
	 * @param  subClasses
	 *            set of subclasses of classItem with their equivalent classes
	 */

	private static void addSubClasses(ClassExpression classItem, Set<Equivalences<ClassExpression>> subClasses) {



		// iterate through all subclasses not only the direct ones
		Iterator<Equivalences<ClassExpression>> classesIt = subClasses.iterator();
		Predicate subClassOf = OBDAVocabulary.RDFS_SUBCLASS;

		while (classesIt.hasNext()) {

			Equivalences<ClassExpression> eqq = classesIt.next();
			Iterator<ClassExpression> itcl = eqq.getMembers().iterator();

			while (itcl.hasNext()) {

				ClassExpression subClassItem = itcl.next();

						/*
						 * Add to datalog the subclasses rdfs:subclass
						 */

				//we do not return blank nodes
//						addBlankNodesRule(subClassItem, classItem, subClassOf);
				addClasses(subClassItem, classItem, subClassOf);

			}

		}

	}

	/**
	 * Add subProperties, equivalences in the datalog program using the DAG with data properties.
	 *
	 */

	private static void addEntailmentsForDataProperties() {

		
		Iterator<Equivalences<DataPropertyExpression>> itProperty = dataPropertyDAG.iterator();



		while (itProperty.hasNext()) {

			Equivalences<DataPropertyExpression> eqv = itProperty.next();
			Set<DataPropertyExpression> equivalentDataProperties = eqv.getMembers();

			Iterator<DataPropertyExpression> iteq = equivalentDataProperties.iterator();

			while (iteq.hasNext()) {

				DataPropertyExpression propertyItem = iteq.next();

				// We need to make sure we make no mappings for Auxiliary roles
				// introduced by the Ontology translation process.
				if (OntologyVocabularyImpl.isAuxiliaryProperty(propertyItem))
					continue;

				// add to datalog program
				// owl:equivalentProperty of data properties

//				log.info ("Add equivalent data properties");

				addDataEquivalences(propertyItem, equivalentDataProperties);



				// add to datalog program
				//rdfs:subproperty of data properties

//				log.info ("Add subdata properties");

				addSubDataProperties(propertyItem, dataPropertyDAG.getSub(eqv));

//				log.info ("Add domain for data properties");
				addDataDomainsFromClasses(propertyItem);

//				log.info ("Add range for data properties");
				addDataRangeFromClasses(propertyItem);



			}

		}

	}

	/**
	 * Called by { @link #addEntailmentsForDataProperties() } add the
	 * owl:equivalentProperty for data properties
	 *
	 * @param  equivalentDataProperties
	 *            set of subproperties  of dataProperty with their equivalent classes
	 */

	private static void addDataEquivalences(DataPropertyExpression dataProperty, Set<DataPropertyExpression> equivalentDataProperties) {


		Predicate equivalent = OBDAVocabulary.OWL_EQUIVALENT_PROPERTY;

		Iterator<DataPropertyExpression> properties = equivalentDataProperties.iterator();

		while (properties.hasNext()) {

			DataPropertyExpression propertyItem2 = properties.next();
			if (OntologyVocabularyImpl.isAuxiliaryProperty(propertyItem2))
				continue;

			/*
			 * Add owl:equivalentProperty to the datalog program
			 */
			addNodesRule(dataProperty.toString(), propertyItem2.toString(), equivalent);


		}

	}


	/**
	 * Called by { @link #addEntailmentsForDataProperties() } add the
	 * rdfs:subproperty for data properties
	 *
	 * @param  subDataProperties
	 *            set of subproperties  of dataProperty with their equivalent classes
	 */
	/**
	 * Add subproperties, equivalences and inverses in the datalog program using the DAG with data properties.
	 *
	 */

	private static void addSubDataProperties(DataPropertyExpression propertyItem, Set<Equivalences<DataPropertyExpression>> subDataProperties) {


		Predicate subPropertyOf = OBDAVocabulary.RDFS_SUBPROPERTY;
		// add all subproperty not only the direct ones
		Iterator<Equivalences<DataPropertyExpression>> propertiesIt = subDataProperties.iterator();

		while (propertiesIt.hasNext()) {

			Equivalences<DataPropertyExpression> eqq = propertiesIt.next();
			Iterator<DataPropertyExpression> itcl = eqq.getMembers().iterator();

			while (itcl.hasNext()) {

				DataPropertyExpression subPropertyItem = itcl.next();

				// We need to make sure we make no mappings for Auxiliary roles
				// introduced by the Ontology translation process.
				if (OntologyVocabularyImpl.isAuxiliaryProperty(subPropertyItem))
					continue;


						/*
						 * Add to datalog program rdfs:subproperty
						 */
				addNodesRule(subPropertyItem.toString(), propertyItem.toString(), subPropertyOf);


			}
		}

	}


	/**
	 * Add subProperties, equivalences and inverses in the datalog program using the DAG with object properties.
	 *
	 */

	private static void addEntailmentsForObjectProperties() {


		Iterator<Equivalences<ObjectPropertyExpression>> itProperty = objectPropertyDAG.iterator();

		while (itProperty.hasNext()) {

			Equivalences<ObjectPropertyExpression> eqv = itProperty.next();

			Set<ObjectPropertyExpression> equivalentObjectProperties = eqv.getMembers();

			Iterator<ObjectPropertyExpression> iteq = equivalentObjectProperties.iterator();

			while (iteq.hasNext()) {

				ObjectPropertyExpression propertyItem = iteq.next();

				// We need to make sure we make no mappings for Auxiliary roles
				// introduced by the Ontology translation process.
				if (OntologyVocabularyImpl.isAuxiliaryProperty(propertyItem))
					continue;

				// add to datalog program owl:inversesOf and
				// owl:equivalentProperty

//				log.info ("Add inverse and equivalent object properties");
				addInversesAndEquivalences(propertyItem, equivalentObjectProperties);

				// add all subproperty not only the direct ones
//				log.info ("Add subobject properties");

				addSubObjectProperties(propertyItem,  objectPropertyDAG.getSub(eqv));

//				log.info ("Add domain for object properties");
				addObjectDomainsFromClasses(propertyItem);

//				log.info ("Add range for object properties");
				addObjectRangeFromClasses(propertyItem);
				
			}

		}

	}
	
	/**
	 * Called by { @link #addEntailmentsForObjectProperties() }
	 * Add inverses and equivalences of  object properties to datalog program
	 * 
	 * @param equivalentObjectProperties
	 */
	private static void addInversesAndEquivalences(ObjectPropertyExpression propertyItem, Set<ObjectPropertyExpression> equivalentObjectProperties) {


		Predicate inverseOf = OBDAVocabulary.OWL_INVERSE;
		Predicate equivalent = OBDAVocabulary.OWL_EQUIVALENT_PROPERTY;

		Iterator<ObjectPropertyExpression> properties = equivalentObjectProperties.iterator();

		while (properties.hasNext()) {

			ObjectPropertyExpression propertyItem2 = properties.next();

			// We need to make sure we make no mappings for Auxiliary roles
			// introduced by the Ontology translation process.
			if (OntologyVocabularyImpl.isAuxiliaryProperty(propertyItem2))
				continue;

			/*
			 * Add owl:equivalentProperty to the datalog program
			 */
			addNodesRule(propertyItem.toString(), propertyItem2.toString(), equivalent);

			ObjectPropertyExpression inverse;

			// add the inverse of the equivalent node
				inverse = propertyItem2.getInverse();

			/*
			 * Add owl:inverseOf to the datalog program
			 */
			addNodesRule(propertyItem.toString(), inverse.toString(), inverseOf);
		}

	}

	 /**
	  * Called by { @link #addEntailmentsForObjectProperties() } add the
	  * rdfs:subproperty for object properties
	  *
	  * @param  subObjectProperties
	  *            set of subproperties  of dataProperty with their equivalent classes
	  */


	private static void addSubObjectProperties(ObjectPropertyExpression propertyItem, Set<Equivalences<ObjectPropertyExpression>> subObjectProperties) {


		Predicate subPropertyOf = OBDAVocabulary.RDFS_SUBPROPERTY;
		// add all subproperties not only the direct ones
		Iterator<Equivalences<ObjectPropertyExpression>> propertiesIt = subObjectProperties.iterator();

		while (propertiesIt.hasNext()) {

			Equivalences<ObjectPropertyExpression> eqq = propertiesIt.next();
			Iterator<ObjectPropertyExpression> itcl = eqq.getMembers().iterator();

			while (itcl.hasNext()) {

				ObjectPropertyExpression subPropertyItem = itcl.next();

				// We need to make sure we make no mappings for Auxiliary roles
				// introduced by the Ontology translation process.
				if (OntologyVocabularyImpl.isAuxiliaryProperty(subPropertyItem))
					continue;

						/*
						 * Add to datalog program rdfs:subproperty
						 */
				addNodesRule(subPropertyItem.toString(), propertyItem.toString(), subPropertyOf);


			}
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

					// add disjoints considering equivalent nodes and subclasses
					Set<Equivalences<ClassExpression>> classes = classDAG.getSub(classDAG.getVertex(disjointElement1));

					for (Equivalences<ClassExpression> equivalentclasses : classes) {

						for (ClassExpression description1 : equivalentclasses.getMembers()) {

							for (ClassExpression disjointElement2 : disjointElements.getComponents()) {


								// a class cannot be disjoint with itself or one of
								// its equivalent classes
								if (!disjointElement1.equals(disjointElement2)) {

									// add disjoints considering equivalent nodes and subclasses
									Set<Equivalences<ClassExpression>> classes2 = classDAG.getSub(classDAG.getVertex(disjointElement2));

									for (Equivalences<ClassExpression> equivalentclasses2 : classes2) {

										for (ClassExpression description2 : equivalentclasses2.getMembers()) {

										/*
										 * Add owl:disjointWith facts to the
										 * datalog program
										 */
//											addBlankNodesRule(description1, description2, disjointClass);
											addClasses(description1, description2, disjointClass);
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

			Set<DataPropertyExpression> disjointElementsComponents = disjointElements.getComponents();
			for(DataPropertyExpression disjointElement1 : disjointElementsComponents) {


				// add disjoints considering equivalent nodes and subclasses

				Set<Equivalences<DataPropertyExpression>> subproperties = dataPropertyDAG.getSub(dataPropertyDAG.getVertex(disjointElement1));

				for (Equivalences<DataPropertyExpression> dataPropertyExpressions : subproperties) {

					for (DataPropertyExpression objectProperty : dataPropertyExpressions.getMembers()) {

						for (DataPropertyExpression disjointElement2 : disjointElementsComponents) {


							// a class cannot be disjoint with itself or one of
							// its equivalent classes
							if (!disjointElement1.equals(disjointElement2)) {

								Set<Equivalences<DataPropertyExpression>> subproperties2 = dataPropertyDAG.getSub(dataPropertyDAG.getVertex(disjointElement2));

								for (Equivalences<DataPropertyExpression> dataPropertyExpressions2 : subproperties2) {

									for (DataPropertyExpression disjointObjectProperty : dataPropertyExpressions2.getMembers()) {

										/*
									 * Add owl:propertyDisjointWith facts to the
									 * datalog program
									 */
										addNodesRule(objectProperty.toString(), disjointObjectProperty.toString(), disjointProperty);


									}

								}

							}


						}
					}
				}
			}
		}

	}

	/**From the disjoint object axioms of the ontology and  and the DAG with properties
	 * returns owl:disjointProperty
	 *
	 * @param disjointObjectPropertiesAxioms
	 */

	private static void addDisjointObjectProperties(List<NaryAxiom<ObjectPropertyExpression>> disjointObjectPropertiesAxioms) {

		Predicate disjointProperty = OBDAVocabulary.OWL_DISJOINT_PROPERTY;

		for (NaryAxiom<ObjectPropertyExpression> disjointElements : disjointObjectPropertiesAxioms) {

			Set<ObjectPropertyExpression> disjointElementsComponents = disjointElements.getComponents();
			for(ObjectPropertyExpression disjointElement1 : disjointElementsComponents) {


				// add disjoints considering equivalent nodes and subclasses

				Set<Equivalences<ObjectPropertyExpression>> subproperties = objectPropertyDAG.getSub(objectPropertyDAG.getVertex(disjointElement1));

				for (Equivalences<ObjectPropertyExpression> objectPropertyExpressions : subproperties) {

					for (ObjectPropertyExpression objectProperty : objectPropertyExpressions.getMembers()) {

						for (ObjectPropertyExpression disjointElement2 : disjointElementsComponents) {


							// a class cannot be disjoint with itself or one of
							// its equivalent classes
							if (!disjointElement1.equals(disjointElement2)) {

								Set<Equivalences<ObjectPropertyExpression>> subproperties2 = objectPropertyDAG.getSub(objectPropertyDAG.getVertex(disjointElement2));

								for (Equivalences<ObjectPropertyExpression> objectPropertyExpressions2 : subproperties2) {

									for (ObjectPropertyExpression disjointObjectProperty : objectPropertyExpressions2.getMembers()) {

										/*
									 * Add owl:propertyDisjointWith facts to the
									 * datalog program
									 */
										addNodesRule(objectProperty.toString(), disjointObjectProperty.toString(), disjointProperty);

										/*
									 * Add owl:propertyDisjointWith facts for
									 * the inverses to the datalog program
									 */

										addNodesRule(objectProperty.getInverse().toString(), disjointObjectProperty.getInverse().toString(), disjointProperty);
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
	 * Add rdfs:domain and rdfs:range. The domain of a property P is given by the  named (OClass)
	 * superclass C of the property some description existsP, the range of a property is given by the domain of the inverse
	 * The method iterates through the DAG with classes. It first searches between
	 * the equivalent nodes (if they are present)  and then it searches in the
	 *  superclasses or subclasses.
	 * 
	 */
	

	private static void addObjectDomainsFromClasses(ObjectPropertyExpression objectPropertyItem ) {


			Equivalences<ClassExpression> domainEquiv = classDAG.getVertex(objectPropertyItem.getDomain());


			Iterator<ClassExpression> iteq = domainEquiv.getMembers().iterator();

			while (iteq.hasNext()) {
				ClassExpression classItem = iteq.next();
				if (classItem instanceof OClass) {

					addNodesRule(objectPropertyItem.toString(), classItem.toString(), OBDAVocabulary.RDFS_DOMAIN);


				}

			}

				/*
			 * Search between superclasses the domain if there were no
			 * named classes in the equivalent nodes.
			 */


			Iterator<Equivalences<ClassExpression>> classesIt2 = classDAG.getSuper(domainEquiv).iterator();

			while (classesIt2.hasNext()) {

				Equivalences<ClassExpression> eqq = classesIt2.next();
				Iterator<ClassExpression> itcl = eqq.getMembers().iterator();

				while (itcl.hasNext()) {
					ClassExpression classItem = itcl.next();
					if (classItem instanceof OClass) {

						addNodesRule(objectPropertyItem.toString(), classItem.toString(), OBDAVocabulary.RDFS_DOMAIN);


					}
				}

			}


	}

	private static void addObjectRangeFromClasses(ObjectPropertyExpression objectPropertyItem ) {



				Equivalences<ClassExpression> rangeEquiv= classDAG.getVertex(objectPropertyItem.getRange());

//				System.out.println(rangeEquiv.getRepresentative());

				Iterator<ClassExpression> iteq2 = rangeEquiv.getMembers().iterator();

				while (iteq2.hasNext()) {
					ClassExpression classItem = iteq2.next();
					if (classItem instanceof OClass){

						addNodesRule(objectPropertyItem.toString(), classItem.toString(), OBDAVocabulary.RDFS_RANGE );


					}

				}



					Iterator<Equivalences<ClassExpression>> classesIt2 = classDAG.getSuper(rangeEquiv).iterator();

					while (classesIt2.hasNext()) {

						Equivalences<ClassExpression> eqq = classesIt2.next();
						Iterator<ClassExpression> itcl = eqq.getMembers().iterator();

						while (itcl.hasNext()) {
							ClassExpression classItem = itcl.next();

							if (classItem instanceof OClass) {

								addNodesRule(objectPropertyItem.toString(), classItem.toString(), OBDAVocabulary.RDFS_RANGE);



							}
						}

					}



			}





	/**
	 * Add rdfs:domain and rdfs:range. It first searches between
	 * the equivalent nodes (if they are present)  and then it searches in the
	 *  superclasses.
	 *
	 */


	private static void addDataDomainsFromClasses(DataPropertyExpression dataPropertyItem ) {


		Equivalences<ClassExpression> domainEquiv = classDAG.getVertex(dataPropertyItem.getDomain());


		Iterator<ClassExpression> iteq = domainEquiv.getMembers().iterator();

		while (iteq.hasNext()) {
			ClassExpression classItem = iteq.next();
			if (classItem instanceof OClass) {

				addNodesRule(dataPropertyItem.toString(), classItem.toString(), OBDAVocabulary.RDFS_DOMAIN);


			}

		}

				/*
			 * Search between superclasses
			 */


		Iterator<Equivalences<ClassExpression>> classesIt2 = classDAG.getSuper(domainEquiv).iterator();

		while (classesIt2.hasNext()) {

			Equivalences<ClassExpression> eqq = classesIt2.next();
			Iterator<ClassExpression> itcl = eqq.getMembers().iterator();

			while (itcl.hasNext()) {
				ClassExpression classItem = itcl.next();
				if (classItem instanceof OClass) {

					addNodesRule(dataPropertyItem.toString(), classItem.toString(), OBDAVocabulary.RDFS_DOMAIN);


				}
			}

		}


	}

	private static void addDataRangeFromClasses(DataPropertyExpression dataPropertyItem) {



		Equivalences<DataRangeExpression> rangeEquiv= dataRangeDAG.getVertex(dataPropertyItem.getRange());

//				System.out.println(rangeEquiv.getRepresentative());

		Iterator<DataRangeExpression> iteq2 = rangeEquiv.getMembers().iterator();

		while (iteq2.hasNext()) {
			DataRangeExpression dataRange = iteq2.next();
			if (dataRange instanceof Datatype){

				addNodesRule(dataPropertyItem.toString(), dataRange.toString(), OBDAVocabulary.RDFS_RANGE );


			}

		}



		Iterator<Equivalences<DataRangeExpression>> datatypesIt2 = dataRangeDAG.getSuper(rangeEquiv).iterator();

		while (datatypesIt2.hasNext()) {

			Equivalences<DataRangeExpression> eqq = datatypesIt2.next();
			Iterator<DataRangeExpression> itcl = eqq.getMembers().iterator();

			while (itcl.hasNext()) {
				DataRangeExpression dataRange = itcl.next();

				if (dataRange instanceof Datatype) {

					addNodesRule(dataPropertyItem.toString(), dataRange.toString(), OBDAVocabulary.RDFS_RANGE);



				}
			}

		}



	}







	/**
	 * We do not return blank nodes
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
		else {
			if (description1 instanceof ObjectSomeValuesFrom || description1 instanceof DataSomeValuesFrom) {
			 // propertySomeDescription
				// add blank node
				terms.add(factory.getConstantBNode(description1.toString()));
			}



		}

		if (description2 instanceof OClass)
		{
			// add URI terms
			terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2.toString())));
		}
		else {
			if  (description2 instanceof ObjectSomeValuesFrom || description2 instanceof DataSomeValuesFrom) {
				// propertySomeDescription
				// add blank node
				terms.add(factory.getConstantBNode(description2.toString()));
			}

		}

		if (terms.size() == 2) {
			Function head = factory.getFunction(function, terms);
			program.add(factory.getCQIE(head));
//			log.debug(head.toString());
		}

	}

	private static void addClasses(ClassExpression description1, ClassExpression description2, Predicate function){
		List<Term> terms = new ArrayList<Term>();

		if (description1 instanceof OClass)
		{
			// add URI terms
			terms.add(factory.getUriTemplate(factory.getConstantLiteral(description1.toString())));
		}
//
		if (description2 instanceof OClass)
		{
			// add URI terms
			terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2.toString())));
		}
//

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
