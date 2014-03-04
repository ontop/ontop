package it.unibz.krdb.obda.subclass;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubClassTest extends TestCase {

		private OBDADataFactory fac;
		private QuestOWLConnection conn;

		Logger log = LoggerFactory.getLogger(this.getClass());
		private OBDAModel obdaModel;
		private OWLOntology ontology;

		final String owlfile = "src/test/resources/subclass/subdescription.owl";
		final String obdafile = "src/test/resources/subclass/subdescriptions-h2.obda";
		private QuestOWL reasoner;
		private Connection sqlConnection;

		@Override
		@Before
		public void setUp() throws Exception {
			try {
				 sqlConnection= DriverManager.getConnection("jdbc:h2:mem:subcountry","sa", "");
				    java.sql.Statement s = sqlConnection.createStatement();
				  
				    try {
				    	String text = new Scanner( new File("src/test/resources/subclass/create-h2.sql") ).useDelimiter("\\A").next();
				    	s.execute(text);
				    	//Server.startWebServer(sqlConnection);
				    	 
				    } catch(SQLException sqle) {
				        System.out.println("Exception in creating db from script");
				    }
				   
				    s.close();
			
			
			// Loading the OWL file
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

			// Loading the OBDA data
			fac = OBDADataFactoryImpl.getInstance();
			obdaModel = fac.getOBDAModel();
			
			ModelIOManager ioManager = new ModelIOManager(obdaModel);
			ioManager.load(obdafile);
		
			QuestPreferences p = new QuestPreferences();
			p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
			// Creating a new instance of the reasoner
			QuestOWLFactory factory = new QuestOWLFactory();
			factory.setOBDAController(obdaModel);

			factory.setPreferenceHolder(p);

			reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

			// Now we are ready for querying
			conn = reasoner.getConnection();
			} catch (Exception exc) {
				try {
					tearDown();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}	
			
		}


		@After
		public void tearDown() throws Exception{
			conn.close();
			reasoner.dispose();
			if (!sqlConnection.isClosed()) {
				java.sql.Statement s = sqlConnection.createStatement();
				try {
					s.execute("DROP ALL OBJECTS DELETE FILES");
				} catch (SQLException sqle) {
					System.out.println("Table not found, not dropping");
				} finally {
					s.close();
					sqlConnection.close();
				}
			}
		}
		

		
		private String runTests(String query) throws Exception {
			QuestOWLStatement st = conn.createStatement();
			String retval;
			try {
				QuestOWLResultSet rs = st.executeTuple(query);
				assertTrue(rs.nextRow());
				OWLIndividual ind1 =	rs.getOWLIndividual("x")	 ;
				retval = ind1.toString();
			} catch (Exception e) {
				throw e;
			} finally {
				try {

				} catch (Exception e) {
					st.close();
				}
				conn.close();
				reasoner.dispose();
			}
			return retval;
		}
		
		public void testDatabase() throws Exception {
			String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country} ORDER BY ?x";
			String val = runTests(query);
			assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Argentina>", val);
		}
}
