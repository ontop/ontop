package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class add facts to the datalog program regarding rdfs:subClassOf and rdfs:subPropertyOf
 * It is possible to activate or deactivate this feature in Quest changing the preferences of
 *  QuestPreferences.ENTAILMENTS_SPARQL
 *
 */
public class SubDescriptionToFactRule {

	private static Logger log = LoggerFactory.getLogger(SubDescriptionToFactRule.class);

	private static OBDADataFactory factory = OBDADataFactoryImpl.getInstance();

	public static void addFacts(DatalogProgram p, Ontology onto) {

		TBoxReasonerImpl reasoner = new TBoxReasonerImpl(onto);
		addSubClassesFromOntology(p, reasoner);
		addSubRolesFromOntology(p, reasoner);

	}

	/**
	 * Add subclasses in the database using the DAG. All subclasses are inserted
	 * considering also equivalences
	 * 
	 * @param conn
	 * @param reasoner
	 * @throws SQLException
	 */

	private static void addSubClassesFromOntology(DatalogProgram p, TBoxReasoner reasoner) {

		EquivalencesDAG<BasicClassDescription> dag = reasoner.getClasses();
		Iterator<Equivalences<BasicClassDescription>> it = dag.iterator();

		while (it.hasNext()) {

			Equivalences<BasicClassDescription> eqv = it.next();

			Iterator<BasicClassDescription> iteq = eqv.getMembers().iterator();

			while (iteq.hasNext()) {

				BasicClassDescription classItem = iteq.next();

				// if we want to add all subrelations not only the direct one
				Iterator<Equivalences<BasicClassDescription>> classesIt = dag
						.getSub(eqv).iterator();

				while (classesIt.hasNext()) {

					Equivalences<BasicClassDescription> eqq = classesIt.next();
					Iterator<BasicClassDescription> itcl = eqq.getMembers()
							.iterator();

					while (itcl.hasNext()) {

						BasicClassDescription subClassItem = itcl.next();

						// added not to consider sub equal to the current same
						// node
						if ((classItem instanceof OClass) && (subClassItem instanceof OClass) && !subClassItem.equals(classItem)) {

							log.debug("Insert class: " + classItem);
							log.debug("SubClass member: " + subClassItem);

							List<Term> terms = new ArrayList<Term>();

							Predicate subClassOf = OBDAVocabulary.RDFS_SUBCLASS;

							// add URI
							terms.add(factory.getUriTemplate(factory.getConstantLiteral(subClassItem.toString())));
							terms.add(factory.getUriTemplate(factory.getConstantLiteral(classItem.toString())));

							Function head = factory.getFunction(subClassOf, terms);

							// log.debug("head: " + head);

							p.appendRule(factory.getCQIE(head));

						}

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

	private static void addSubRolesFromOntology(DatalogProgram p, TBoxReasoner reasoner) {

		EquivalencesDAG<Property> dag = reasoner.getProperties();
		Iterator<Equivalences<Property>> it = dag.iterator();

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
						if ((!propertyItem.isInverse()) && (!subPropertyItem.isInverse()) && !subPropertyItem.equals(propertyItem)) {

							log.debug("Insert property: " + propertyItem);
							log.debug("SubProperty member: " + subPropertyItem);

							List<Term> terms = new ArrayList<Term>();

							Predicate subPropertyOf = OBDAVocabulary.RDFS_SUBPROPERTY;

							// add URI terms
							terms.add(factory.getUriTemplate(factory.getConstantLiteral(subPropertyItem.toString())));
							terms.add(factory.getUriTemplate(factory.getConstantLiteral(propertyItem.toString())));

							Function head = factory.getFunction(subPropertyOf, terms);

							log.debug("head " + head);

							p.appendRule(factory.getCQIE(head));

						}

					}
				}
			}

		}

	}
}
