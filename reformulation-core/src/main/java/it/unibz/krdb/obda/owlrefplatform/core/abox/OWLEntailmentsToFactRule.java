package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.TBoxTraversal;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.TBoxTraverseListener;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.VocabularyExtractor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

	private static DatalogProgram program;

	public static void addFacts(DatalogProgram p, Ontology onto, Map<Predicate, Description> equivalenceMaps) {

		program = p;

		TBoxReasoner reasoner = new TBoxReasonerImpl(onto);
//		addSubClassesFromOntology(reasoner);
//		addSubRolesFromOntology(reasoner);
		addEquivalences(equivalenceMaps);
		addRangeFromOntology(reasoner);

		 TBoxTraversal.traverse(reasoner, new TBoxTraverseListener() {
			  
			  @Override public void onInclusion(Property sub, Property sup) {
			 
			  
			  if ((!sub.isInverse()) && (!sup.isInverse()) ) {
//				  createFact(sub,sup, OBDAVocabulary.RDFS_SUBPROPERTY);
				  addNodesRule(sub.toString(), sup.toString(), OBDAVocabulary.RDFS_SUBPROPERTY);
				 
			  }
			  
			  }
			  
			  @Override public void onInclusion(BasicClassDescription sub, BasicClassDescription sup) {
			  
			  
//			  if ((sub instanceof OClass) && (sup instanceof OClass))
//			  createFact(sub,sup, OBDAVocabulary.RDFS_SUBCLASS); }
				  addBlankNodesRule(sub, sup, OBDAVocabulary.RDFS_SUBCLASS);
				 
			  }
			  
//			  public void createFact(Description sub, Description sup, Predicate
//			  subPropertyOf ){
//			  
//			  
//			  List<Term> terms = new ArrayList<Term>();
//			  
//			  // add URI terms
//			  terms.add(factory.getUriTemplate(factory.getConstantLiteral
//			  (sub.toString())));
//			  terms.add(factory.getUriTemplate(factory.getConstantLiteral
//			  (sup.toString())));
//			  
//			  Function head = factory.getFunction(subPropertyOf, terms);
//			  
//			  log.debug("head " + head);
//			  
//			  p.appendRule(factory.getCQIE(head)); }
//			  
//			  
			  }, true);
		
//		
		 
	}

	private static void addEquivalences(Map<Predicate, Description> equivalenceMaps) {
		
		Iterator<Entry<Predicate, Description>> itEquivalences = equivalenceMaps.entrySet().iterator();
		Predicate equivalentClass = OBDAVocabulary.OWL_EQUIVALENT;

		while (itEquivalences.hasNext()) {

			Entry<Predicate, Description> map = itEquivalences.next();

			Predicate key = map.getKey();
			Description value = map.getValue();

			if (key.isClass()) {

				addNodesRule(key.getName(), value.toString(), equivalentClass);

			}
		}

	}

	/**
	 * Add subclasses in the database using the DAG. All subclasses are inserted
	 * considering also equivalences
	 * 
	 * @param conn
	 * @param reasoner
	 * @throws SQLException
	 */

	private static void addSubClassesFromOntology(TBoxReasoner reasoner) {

		EquivalencesDAG<BasicClassDescription> dag = reasoner.getClasses();
		Iterator<Equivalences<BasicClassDescription>> it = dag.iterator();
		Predicate subClassOf = OBDAVocabulary.RDFS_SUBCLASS;

		while (it.hasNext()) {

			Equivalences<BasicClassDescription> eqv = it.next();

			Iterator<BasicClassDescription> iteq = eqv.getMembers().iterator();

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

						addBlankNodesRule( subClassItem, classItem, subClassOf);
						
						

						log.debug("Insert class: " + classItem);
						log.debug("SubClass member: " + subClassItem);

					}

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
	
	private static void addRangeFromOntology(TBoxReasoner reasoner) {

		EquivalencesDAG<BasicClassDescription> dag = reasoner.getClasses();
		Iterator<Equivalences<BasicClassDescription>> it = dag.iterator();

		while (it.hasNext()) {

			Equivalences<BasicClassDescription> eqv = it.next();

			Iterator<BasicClassDescription> iteq = eqv.getMembers().iterator();

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

						addRangeOrDomainRule(subClassItem, classItem);
									

						log.debug("Insert class: " + classItem);
						log.debug("SubClass member: " + subClassItem);

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

	public static void addNodesRule(String description1, String description2, Predicate function) {

		List<Term> terms = new ArrayList<Term>();

		// add URI terms
		terms.add(factory.getUriTemplate(factory.getConstantLiteral(description1)));
		terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2)));

		Function head = factory.getFunction(function, terms);

		log.debug("head: " + head);

		program.appendRule(factory.getCQIE(head));

	}

	/**
	 * Create a fact given two blank nodes and a predicate
	 * 
	 * @param description1
	 * @param description2
	 * @param function
	 */

	public static void addBlankNodesRule(BasicClassDescription description1, BasicClassDescription description2, Predicate function) {

		List<Term> terms = new ArrayList<Term>();

		if (description1 instanceof OClass)
			// add URI terms
			terms.add(factory.getUriTemplate(factory.getConstantLiteral(description1.toString())));

		else if (description1 instanceof PropertySomeRestriction) {
			// add blank node
			terms.add(factory.getConstantBNode(description1.toString()));
		}

		if (description2 instanceof OClass)
			// add URI terms
			terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2.toString())));

		else if (description2 instanceof PropertySomeRestriction) {

			// add blank node
			terms.add( factory.getConstantBNode(description2.toString()));
		}

		if (terms.size() == 2) {
			Function head = factory.getFunction(function, terms);
			program.appendRule(factory.getCQIE(head));
		}

	}
	
	public static void addRangeOrDomainRule(BasicClassDescription description1, BasicClassDescription description2) {

		List<Term> terms = new ArrayList<Term>();

		if (description1 instanceof PropertySomeRestriction && description2 instanceof OClass) {
			// get the property related to the existential
			Predicate property1 = ((PropertySomeRestriction) description1).getPredicate();

			if (((PropertySomeRestriction) description1).isInverse())
			{
				terms.add(factory.getUriTemplate(factory.getConstantLiteral(property1.getName())));
				terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2.toString())));
				Function head = factory.getFunction(OBDAVocabulary.RDFS_RANGE, terms);
				program.appendRule(factory.getCQIE(head));

			}
			else
			{
				terms.add(factory.getUriTemplate(factory.getConstantLiteral(description1.toString())));
				terms.add(factory.getUriTemplate(factory.getConstantLiteral(description2.toString())));
				Function head = factory.getFunction(OBDAVocabulary.RDFS_DOMAIN, terms);
				program.appendRule(factory.getCQIE(head));
			}
		}
		

	}
	
	
	
	

}
