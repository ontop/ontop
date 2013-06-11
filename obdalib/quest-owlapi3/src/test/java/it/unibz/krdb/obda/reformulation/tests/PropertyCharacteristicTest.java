package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
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
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class PropertyCharacteristicTest extends TestCase {
	
	private OWLConnection conn = null;
	private OWLStatement stmt = null;
	private QuestOWL reasoner = null;
	
	private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
	private static 	QuestPreferences prefs;
	static {
		prefs = new QuestPreferences();
		prefs.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		prefs.setCurrentValueOf(QuestPreferences.REWRITE, QuestConstants.FALSE);
	}
	
	public void testNoProperty() throws Exception {
		final File owlFile = new File("src/test/resources/property-characteristics/noproperty.owl");
		final File obdaFile = new File("src/test/resources/property-characteristics/noproperty.obda");
		
		setupReasoner(owlFile, obdaFile);
		OWLResultSet rs = executeQuery("" +
				"PREFIX : <http://www.semanticweb.org/johardi/ontologies/2013/3/Ontology1365668829973.owl#> \n" +
				"SELECT ?x ?y \n" +
				"WHERE { ?x :knows ?y . }"
				);
		final int count = countResult(rs, true);
		assertEquals(3, count);
	}
	
	public void testSymmetricProperty() throws Exception {
		final File owlFile = new File("src/test/resources/property-characteristics/symmetric.owl");
		final File obdaFile = new File("src/test/resources/property-characteristics/symmetric.obda");
		
		setupReasoner(owlFile, obdaFile);
		OWLResultSet rs = executeQuery("" +
				"PREFIX : <http://www.semanticweb.org/johardi/ontologies/2013/3/Ontology1365668829973.owl#> \n" +
				"SELECT ?x ?y \n" +
				"WHERE { ?x :knows ?y . }"
				);
		final int count = countResult(rs, true);
		assertEquals(6, count);
	}
	
	private void setupReasoner(File owlFile, File obdaFile) throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(owlFile);
	
		OBDAModel obdaModel = ofac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdaFile);
		
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(prefs);
		
		reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
	}
	
	private OWLResultSet executeQuery(String sparql) {
		try {
			conn = reasoner.getConnection();
			stmt = conn.createStatement();
			return stmt.executeTuple(sparql);
		} catch (OWLException e) {
			System.err.println(e.getMessage());
		} catch (OBDAException e) {
			System.err.println(e.getMessage());
		}
		throw new RuntimeException();
	}
	
	@Override
	public void tearDown() {
		try {
			if (stmt != null && !stmt.isClosed()) {
				stmt.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
			if (reasoner != null) {
				reasoner.dispose();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	private int countResult(OWLResultSet rs, boolean stdout) throws OWLException {
		int counter = 0;
		while (rs.nextRow()) {
			counter++;
			if (stdout) {
				for (int column = 1; column <= rs.getColumCount(); column++) {
					OWLObject binding = rs.getOWLObject(column);
					System.out.print(binding.toString() + ", ");
				}
				System.out.print("\n");
			}
		}
		return counter;
	}
}
