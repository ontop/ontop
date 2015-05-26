/**
 * 
 */
package it.unibz.krdb.sql;

import static org.junit.Assert.*;
import it.unibz.krdb.obda.model.OBDAModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import org.junit.After;
import org.junit.Test;

/**
 * @author dagc
 *
 */
public class InequalitiesSatisfiabilityTest2 {

/*	public void test() {
		this.start_reasoner(uc_owlfile, uc_obdafile, uc_create);
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x a :A. ?x a :B .}";
		QuestOWLStatement st = conn.createStatement();
		
		
		String sql = st.getUnfolding(query);
		boolean m = sql.matches("(?ms)(.*)\"TABLE1\"(.*),(.*)\"TABLE1\"(.*)");
		assertTrue(m);
		
	
		
		
		// io dovrei chiamare
		// DatalogProgram getUnfolding(DatalogProgram query)
		// unfolder = new QuestUnfolder(unfoldingOBDAModel, metadata, localConnection, sourceId);
		
		// new QuestStatement(Quest questinstance, QuestConnection conn, Statement st)
		//DatalogProgram query = null;
		//QuestStatement qs = new QuestStatement(null, null, null);
		//DatalogProgram result = qs.getUnfolding(query);
		// taken from leftjoinunfoldingtest
		// A program that unifies with A, but not R, y should become null
		/*
		DatalogProgram p = fac.getDatalogProgram();
		Function body = fac.getFunction(fac.getPredicate("T1", 2), fac.getVariable("x"), fac.getVariable("y"));
		Function head = fac.getFunction(fac.getPredicate("A", 1), fac.getVariable("x"));
		CQIE rule2 = fac.getCQIE(head, body);
		p.appendRule(rule2);

		DatalogProgram query = fac.getDatalogProgram();
		// main rule q(x,y) :- LJ(A(x), R(x,y))
		Function a = fac.getFunction(fac.getClassPredicate("A"), fac.getVariable("x"));
		Function R = fac.getFunction(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
		Function lj = fac.getSPARQLLeftJoin(a, R);
		head = fac.getFunction(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE rule1 = fac.getCQIE(head, lj);
		query.appendRule(rule1);

		DatalogUnfolder unfolder = new DatalogUnfolder(p.getRules());
		DatalogProgram result = unfolder.unfold(query, "q");
		*/
	//}*/
	
	/* test with sql */
	
	// 	see TestQuestImplicitDBConstraints.java
	
	
	Quest quest;
	static String  in_owlfile = "src/test/resources/inequalities/in.owl" ;
	static String in_obdafile = "src/test/resources/inequalities/in.obda";

	private OBDADataFactory fac;
	private QuestOWLConnection conn;
	private QuestOWLFactory factory;
	
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	private QuestOWL reasoner;
	private Connection sqlConnection;

	Logger log = LoggerFactory.getLogger(this.getClass());
	
	public boolean start_reasoner() throws Exception {
		try {
			sqlConnection = DriverManager.getConnection("jdbc:h2:mem:countries","sa", "");
			java.sql.Statement s = sqlConnection.createStatement();

			try {
				s.execute("CREATE TABLE TABLE (\"COL1\" INT, \"COL2\" INT);");
			} catch(SQLException sqle) {
				log.debug("Exception in creating db from script");
				throw sqle;
				//return false;
			}

			s.close();


			// Loading the OWL file
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			ontology = manager.loadOntologyFromOntologyDocument((new File(in_owlfile)));

			// Loading the OBDA data
			fac = OBDADataFactoryImpl.getInstance();
			obdaModel = fac.getOBDAModel();

			ModelIOManager ioManager = new ModelIOManager(obdaModel);
			ioManager.load(in_obdafile);

			QuestPreferences p = new QuestPreferences();
			p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);

			
			// Creating a new instance of the reasoner
			this.factory = new QuestOWLFactory();
			factory.setOBDAController(obdaModel);

			factory.setPreferenceHolder(p);

		} catch (Exception exc) {
			throw exc;/*
			try {
				tearDown();
			} catch (Exception e2) {
				throw e2;
				e2.printStackTrace();
				log.debug(e2.getStackTrace().toString());
				return false;
			}
			return false;*/
		}	
		return true;
	}


	@After
	public void tearDown() throws Exception {
		if (conn != null)
			conn.close();
		if (reasoner != null)
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
	
	private String unfold(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		return st.getUnfolding("PREFIX : <http://www.semanticweb.org/ontologies/2015/5/untitled-ontology-1636#> SELECT * WHERE {" + query + "}");
	}
	
	@Test
	public void test_1() throws Exception {
		System.out.println("test started :)");
		assertTrue(this.start_reasoner());
		
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
		this.conn = reasoner.getConnection();
		
		assertEquals(unfold("?x :Gt ?y; ?y :Gt ?x .").length(), 0);
		
		assertEquals(unfold("?x :Gt ?y; ?y :Gt ?x .").length(), 0);
		
		assertEquals(unfold("?x :Gt ?y; ?y :Gt ?x .").length(), 0);
	}


}
