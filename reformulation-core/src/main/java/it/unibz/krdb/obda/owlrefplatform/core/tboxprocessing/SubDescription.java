package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;


import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubDescription {

	private final static String insert_subrelation = "INSERT INTO subrelation (description, subdescription) VALUES(?, ?)";
	private static Logger log = LoggerFactory.getLogger(SubDescription.class);
	private static Connection conn;
	private static OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	/**
	 * Add subclasses in the database using the DAG.
	 * All subclasses are inserted considering also equivalences
	 * @param conn
	 * @param reasoner
	 * @throws SQLException
	 */
	
	private static void addSubclassesFromOntology(Connection conn,TBoxReasoner reasoner) throws SQLException {
		// statement for the inserts
		PreparedStatement insertStm = conn.prepareStatement(insert_subrelation);

		EquivalencesDAG<BasicClassDescription> dag = reasoner.getClasses();
		Iterator<Equivalences<BasicClassDescription>> it = dag.iterator();

		while (it.hasNext()) {
			Equivalences<BasicClassDescription> eqv = it.next();

			Iterator<BasicClassDescription> iteq = eqv.getMembers().iterator();

			while (iteq.hasNext()) {

				BasicClassDescription classItem = iteq.next();
				log.debug("New class member: " + classItem);

				// Iterator<Equivalences<BasicClassDescription>> classesIt = dag
				// .getDirectSub(eqv).iterator();

				// if we want to add all subrelations not only the direct one
				Iterator<Equivalences<BasicClassDescription>> classesIt = dag
						.getSub(eqv).iterator();
				while (classesIt.hasNext()) {
					Equivalences<BasicClassDescription> eqq = classesIt.next();
					Iterator<BasicClassDescription> itcl = eqq.getMembers()
							.iterator();

					while (itcl.hasNext()) {

						BasicClassDescription subClassItem = itcl.next();
						// added not to consider sub of the same node
						if (!subClassItem.equals(classItem)) {

							log.debug("Insert class: " + classItem);
							log.debug("SubClass member: " + subClassItem);
							insertStm.setString(1, classItem.toString());
							insertStm.setString(2, subClassItem.toString());
							insertStm.executeUpdate();
						}

					}
				}
			}
			conn.commit();
		}

	}
	
	/**
	 * Add subproperties in the database using the DAG.
	 * All subproperties are inserted considering also equivalences
	 * @param conn
	 * @param reasoner
	 * @throws SQLException
	 */
	private static void addSubrolesFromOntology(Connection conn,
			TBoxReasoner reasoner) throws SQLException {
		// statement for the inserts
		PreparedStatement insertStm = conn.prepareStatement(insert_subrelation);

		EquivalencesDAG<Property> dag = reasoner.getProperties();
		Iterator<Equivalences<Property>> it = dag.iterator();

		while (it.hasNext()) {
			Equivalences<Property> eqv = it.next();

			Iterator<Property> iteq = eqv.getMembers().iterator();

			while (iteq.hasNext()) {

				Property propertyItem = iteq.next();
				log.debug("New property member: " + propertyItem);

				Iterator<Equivalences<Property>> propertyIt = dag.getDirectSub(
						eqv).iterator();

				// if we want all combinations
				// Iterator<Equivalences<Property>> propertyIt = dag
				// .getSub(eqv).iterator();

				while (propertyIt.hasNext()) {

					Equivalences<Property> eqq = propertyIt.next();
					Iterator<Property> itcl = eqq.getMembers().iterator();

					while (itcl.hasNext()) {

						Property subPropertyItem = itcl.next();
						// added not to consider sub of the same node

						if (!subPropertyItem.equals(propertyItem)) {
							log.debug("Insert property: " + propertyItem);
							log.debug("SubProperty member: " + subPropertyItem);
							insertStm.setString(1, propertyItem.toString());
							insertStm.setString(2, subPropertyItem.toString());
							insertStm.executeUpdate();
						}

					}
				}
			}
			conn.commit();
		}

	}

	/**
	 * Store the information about subclasses and subroles in the database of the user.
	 * Creates a new table subrelation that contains the description (class or property) and the subdescription (subproperty and subclass)
	 * @param conn
	 * @param onto
	 * @throws SQLException
	 */
	public static void storeData(Connection connection, Ontology onto) throws SQLException {

		conn=connection;
		Statement stmt = null;

		// Obtain the statement object for query execution
		
		
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS subrelation (description VARCHAR(500) NOT NULL, subdescription VARCHAR(500) NOT NULL)");
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Problem creating the internal table to store subrelations "+ e.getMessage());
		}
		
		

		TBoxReasonerImpl reasoner = new TBoxReasonerImpl(onto);
		
		conn.setAutoCommit(false);
		
		addSubclassesFromOntology(conn, reasoner);
//		addSubrolesFromOntology(conn,reasoner);
		
		conn.setAutoCommit(true);

	}
	
	
	//TODO remove this part
	/**ONLY FOR TEST
	 * Store the information about subclasses and subroles in an H2 database in memory.
	 * Creates a new table subrelation that contains the description (class or property) and the subdescription (subproperty and subclass)
	 * @param conn
	 * @param onto
	 * @throws SQLException
	 */
	@Deprecated
	public static Connection storeDataH2( Ontology onto) throws SQLException {

	    conn= DriverManager.getConnection("jdbc:h2:mem:subrelation","sa", "");
//		conn= DriverManager.getConnection("jdbc:h2:~/test","sa", "");
		
		Statement stmt = null;

		// Obtain the statement object for query execution
		stmt = conn.createStatement();

		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS subrelation (description VARCHAR(500) NOT NULL, subdescription VARCHAR(500) NOT NULL)");
		
		stmt.close();

		TBoxReasonerImpl reasoner = new TBoxReasonerImpl(onto);
		conn.setAutoCommit(false);
		addSubclassesFromOntology(conn, reasoner);
		addSubrolesFromOntology(conn,reasoner);

		conn.setAutoCommit(true);
		return conn;

	}
	
	public static void dropTable() throws SQLException{
	
		Statement stmt = null;

		// Obtain the statement object for query execution
		stmt = conn.createStatement();

		stmt.executeUpdate("DROP TABLE subrelation");
		
		stmt.close();
	}
	
	
	/**
	 * create the mappings for subclass
	 * @param mappings 
	 */
			
	public static OBDAMappingAxiom addMapping(ArrayList<OBDAMappingAxiom> mappings){
		
			OBDAMappingAxiom mapping ;
//				CQIE targetQuery = (CQIE) mapping.getTargetQuery();
//				List<Function> body = targetQuery.getBody();
			Function head = null ;
			//To construct the head,
			List<Term> terms = new ArrayList<Term>();
			
//			for(int i=0;i<table.countAttribute();i++){
//					headTerms.add(df.getVariable(table.getAttributeName(i+1)));
//				}
			terms.add(dfac.getVariable("description"));
			terms.add(dfac.getVariable("subdescription"));
			Predicate headPredicate = dfac.getPredicate("http://obda.inf.unibz.it/quest/vocabulary#q", terms.size());
			head = dfac.getFunction(headPredicate, terms);
				
			List<Function> body = new LinkedList<Function>();
			Predicate subSelect = dfac.getObjectPropertyPredicate(OBDAVocabulary.RDFS_SUBCLASS_URI);
			Function atom = dfac.getFunction(subSelect, terms);
			body.add(atom);
			

//				
			CQIE newTargetQuery = dfac.getCQIE(head, body);
			mapping =dfac.getRDBMSMappingAxiom("id","SELECT description, subdescription FROM subrelation", newTargetQuery);
			
			mappings.add(mapping);
			
			return mapping;
		
	}
	
//	public static DatalogProgram createDatalog(Ontology onto){
//		TBoxReasonerImpl reasoner = new TBoxReasonerImpl(onto);
//		return  dfac.getDatalogProgram(rules);
//		
//	}
		
	


}
