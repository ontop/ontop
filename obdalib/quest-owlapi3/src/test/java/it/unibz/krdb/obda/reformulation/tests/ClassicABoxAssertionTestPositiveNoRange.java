package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLConnection;
import it.unibz.krdb.obda.owlapi3.OWLResultSet;
import it.unibz.krdb.obda.owlapi3.OWLStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;

import java.io.File;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/***
 * This test check proper handling of ABox assertions, including handling of the
 * supported data types. We check that each ABox assertion is inserted in the
 * database and the data is taken into account in relevant queries. Typing is
 * important in that although all data will be entered, not all data
 * participates in all queries.
 * 
 * @author mariano
 * 
 */
public class ClassicABoxAssertionTestPositiveNoRange extends TestCase {

	QuestOWL reasoner = null;
	private OWLConnection conn;
	private OWLStatement st;

	public ClassicABoxAssertionTestPositiveNoRange() throws Exception {
		QuestPreferences pref = new QuestPreferences();
		pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		String owlfile = "src/test/resources/test/owl-types-simple-split.owl";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
		OBDAModel apic = obdafac.getOBDAModel();
//		String obdafile = owlfile.substring(0, owlfile.length() - 3) + "obda";

//		DataManager ioManager = new DataManager(apic);
//		ioManager.loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getOntologyID().getOntologyIRI().toURI(),
//				apic.getPrefixManager());
		QuestOWLFactory fac = new QuestOWLFactory();
		fac.setOBDAController(apic);
		fac.setPreferenceHolder(pref);

		reasoner = (QuestOWL) fac.createReasoner(ontology);
		reasoner.flush();

		conn = reasoner.getConnection();
		st = conn.createStatement();
	}

	private int executeQuery(String q) throws OWLException {
		String prefix = "PREFIX : <http://it.unibz.krdb/obda/ontologies/quest-typing-test.owl#> \n PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
		String query = prefix + " " + q;

		OWLResultSet res = st.execute(query);
		int count = 0;
		int columns = res.getColumCount();
		while (res.nextRow()) {
			for (int i = 0; i < columns; i++) {
				OWLObject o = res.getOWLObject(i+1);
				System.out.println(o.toString());
			}
			count += 1;
		}

		res.close();
		return count;

	}

	public void testClassAssertions() throws OWLException {
		String query = "SELECT ?x WHERE {?x a :class}";
		int count = executeQuery(query);
		assertEquals(1, count);
	}

	public void testObjectPropertyAssertions() throws OWLException{
		String query = "SELECT ?x ?y WHERE {?x :oproperty ?y}";
		int count = executeQuery(query);
		assertEquals(1, count);
		
		
	}

	public void testDataPropertyAssertionsLiteral() throws OWLException{
		String query = "SELECT ?x WHERE {?x :uliteral ?y}";
		int count = executeQuery(query);
		assertEquals(2, count);
	}

	public void testDataPropertyAssertionsBoolean() throws OWLException{
		String query = "SELECT ?x WHERE {?x :uboolean ?y}";
		int count = executeQuery(query);
		assertEquals(4, count);
	}

	public void testDataPropertyAssertionsByte() throws OWLException{

	}

	public void testDataPropertyAssertionsDatetime() throws OWLException{
		String query = "SELECT ?x WHERE {?x :udateTime ?y}";
		int count = executeQuery(query);
		assertEquals(5, count);
	}

	public void testDataPropertyAssertionsDecimal() throws OWLException{
		String query = "SELECT ?x WHERE {?x :udecimal ?y}";
		int count = executeQuery(query);
		assertEquals(8, count);
	}

	public void testDataPropertyAssertionsDouble() throws OWLException{
		String query = "SELECT ?x WHERE {?x :udouble ?y}";
		int count = executeQuery(query);
		assertEquals(9, count);
	}

	public void testDataPropertyAssertionsFloat() throws OWLException{
		String query = "SELECT ?x WHERE {?x :ufloat ?y}";
		int count = executeQuery(query);
		assertEquals(9, count);
	}

	public void testDataPropertyAssertionsInt() throws OWLException{
		String query = "SELECT ?x ?y WHERE {?x :uint ?y}";
		int count = executeQuery(query);
		assertEquals(6, count);
		
		query = "SELECT ?x ?y WHERE {?x :uint ?y FILTER (?y > 0)}";
		count = executeQuery(query);
		assertEquals(3, count);
	}

	public void testDataPropertyAssertionsInteger()throws OWLException {
		String query = "SELECT ?x WHERE {?x :uinteger ?y}";
		int count = executeQuery(query);
		assertEquals(5, count);
	}

	public void testDataPropertyAssertionsLong() throws OWLException{
		String query = "SELECT ?x WHERE {?x :ulong ?y}";
		int count = executeQuery(query);
		assertEquals(6, count);
	}

	public void testDataPropertyAssertionsShort()throws OWLException {
		
	}

}
