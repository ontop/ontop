package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubDescriptionToFactRule {
	
	private static Logger log = LoggerFactory.getLogger(ABoxToFactRuleConverter.class); 

	private static OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
	
	
	public static void addFacts(DatalogProgram p, Ontology onto) {
		
		TBoxReasonerImpl reasoner = new TBoxReasonerImpl(onto);
		addSubclassesFromOntology(p, reasoner);
//		addSubRolesFromOntology(reasoner);
		
	}

	/**
	 * Add subclasses in the database using the DAG.
	 * All subclasses are inserted considering also equivalences
	 * @param conn
	 * @param reasoner
	 * @throws SQLException
	 */
	
	private static void addSubclassesFromOntology(DatalogProgram p, TBoxReasoner reasoner) {

		EquivalencesDAG<BasicClassDescription> dag = reasoner.getClasses();
		Iterator<Equivalences<BasicClassDescription>> it = dag.iterator();

		while (it.hasNext()) {
			
			Equivalences<BasicClassDescription> eqv = it.next();

			Iterator<BasicClassDescription> iteq = eqv.getMembers().iterator();

			while (iteq.hasNext()) {

				BasicClassDescription classItem = iteq.next();
				log.debug("New class member: " + classItem);

//				 Iterator<Equivalences<BasicClassDescription>> classesIt = dag
//				 .getDirectSub(eqv).iterator();

				// if we want to add all subrelations not only the direct one
				Iterator<Equivalences<BasicClassDescription>> classesIt = dag
						.getSub(eqv).iterator();
				
				while (classesIt.hasNext()) {
					
					Equivalences<BasicClassDescription> eqq = classesIt.next();
					Iterator<BasicClassDescription> itcl = eqq.getMembers()
							.iterator();

					while (itcl.hasNext()) {

						BasicClassDescription subClassItem = itcl.next();
						
						// added not to consider sub equal to the current same node
						if (!subClassItem.equals(classItem)) {

							log.debug("Insert class: " + classItem);
							log.debug("SubClass member: " + subClassItem);
							
							List<Term> terms = new ArrayList<Term>();
													
							Predicate subClassOf = factory.getObjectPropertyPredicate(OBDAVocabulary.RDFS_SUBCLASS_URI);
//							Predicate classPredicate = factory.getClassPredicate(classItem.toString());
//							Predicate subclassPredicate = factory.getClassPredicate(subClassItem.toString());
							
							
							terms.add(factory.getConstantLiteral(subClassItem.toString()));
							terms.add(factory.getConstantLiteral(classItem.toString()));
				
							Function head = factory.getFunction(subClassOf, terms);
							
							System.out.println(head);
							
							
							p.appendRule(factory.getCQIE(head, new LinkedList<Function>()));
							
							
						}

					}
				}
			}
			
		}

	}
}

