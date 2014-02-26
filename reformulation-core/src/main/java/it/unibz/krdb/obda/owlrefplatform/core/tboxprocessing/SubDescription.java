package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;


import it.unibz.krdb.obda.ontology.BasicClassDescription;
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
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubDescription {

	private final static String insert_subrelation = "INSERT INTO subrelation (description, subdescription) VALUES(?, ?)";
	private static Logger log = LoggerFactory.getLogger(SubDescription.class);
	private static Connection conn;

	/**
	 * Add subclasses in the database using the DAG.
	 * All subclasses are inserted considering also equivalences
	 * @param conn
	 * @param reasoner
	 * @throws SQLException
	 */
	
	private static void addSubclassesFromOntology(Connection conn,
			TBoxReasoner reasoner) throws SQLException {
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

				Iterator<Equivalences<BasicClassDescription>> classesIt = dag
						.getDirectSub(eqv).iterator();
				while (classesIt.hasNext()) {
					Equivalences<BasicClassDescription> eqq = classesIt.next();
					Iterator<BasicClassDescription> itcl = eqq.getMembers()
							.iterator();
					while (itcl.hasNext()) {

						log.debug("Insert class: " + classItem);
						BasicClassDescription subClassItem = itcl.next();
						log.debug("SubClass member: " + subClassItem);
						insertStm.setString(1, classItem.toString());
						insertStm.setString(2, subClassItem.toString());
						insertStm.executeUpdate();

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
				

				Iterator<Equivalences<Property>> classesIt = dag
						.getDirectSub(eqv).iterator();
				while (classesIt.hasNext()) {
					
					Equivalences<Property> eqq = classesIt.next();
					Iterator<Property> itcl = eqq.getMembers().iterator();
					
					while (itcl.hasNext()) {

						log.debug("Insert property: " + propertyItem);
						Property subPropertyItem = itcl.next();
						log.debug("SubProperty member: " + subPropertyItem);
						insertStm.setString(1, propertyItem.toString());
						insertStm.setString(2, subPropertyItem.toString());
						insertStm.executeUpdate();

						
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
	public static void storeData(Connection connection, Ontology onto)
			throws SQLException {

		conn=connection;
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

	}
	
	/**
	 * Store the information about subclasses and subroles in an H2 database in memory.
	 * Creates a new table subrelation that contains the description (class or property) and the subdescription (subproperty and subclass)
	 * @param conn
	 * @param onto
	 * @throws SQLException
	 */
	
	public static Connection storeDataH2( Ontology onto)
			throws SQLException {

		Connection conn= DriverManager.getConnection("jdbc:h2:mem:subrelation","sa", "");
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


}
