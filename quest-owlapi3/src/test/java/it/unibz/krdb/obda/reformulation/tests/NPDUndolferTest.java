package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.QueryIOManager;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import java.io.File;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NPDUndolferTest extends TestCase {

	
	private final String owlfile = "src/test/resources/npd-v2-ql_a.owl";

	private OWLOntology ontology;
	private OWLOntologyManager manager;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public NPDUndolferTest() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));
	}

	public void test3InitializingQuest() throws Exception {
	
		QuestOWLFactory fac = new QuestOWLFactory();

		QuestPreferences pref = new QuestPreferences();
		pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);

		fac.setPreferenceHolder(pref);

		QuestOWL quest = fac.createReasoner(ontology);
		QuestOWLConnection qconn =  quest.getConnection();
		QuestOWLStatement st = qconn.createStatement();
		
		quest.getQuestInstance().getSemanticIndexRepository().createIndexes(qconn.getConnection());
		

		String query =
"PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
"PREFIX isc: <http://resource.geosciml.org/classifier/ics/ischart/>" +
"PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>" +
"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
"PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
"PREFIX void: <http://rdfs.org/ns/void#>" +
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
"PREFIX ex: <http://example.org/ex#>" +
"PREFIX quest: <http://obda.org/quest#>" +
"PREFIX diskos: <http://sws.ifi.uio.no/data/diskos/>" +
"PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>" +
"PREFIX ptl: <http://sws.ifi.uio.no/vocab/npd-v2-ptl#>" +
"PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
"PREFIX geos: <http://www.opengis.net/ont/geosparql#>" +
"PREFIX sql: <http://sws.ifi.uio.no/vocab/sql#>" +
"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
"PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
"PREFIX diskosv: <http://sws.ifi.uio.no/vocab/diskos#>" +
"SELECT DISTINCT ?wellbore (?length AS ?lenghtM) ?company ?year " +
"WHERE {" +
"  ?wc npdv:coreForWellbore" +
"        [ rdf:type                      npdv:Wellbore ;" +
"          npdv:name                     ?wellbore ;" +
"          npdv:wellboreCompletionYear   ?year ;" +
"          npdv:drillingOperatorCompany  [ npdv:name ?company ] " +
"        ] ." +
"  { ?wc npdv:coresTotalLength ?length } " +
"  " +
"  FILTER(?year >= \"2008\"^^xsd:integer &&" +
"         ?length > 50 " +
"  )" +
"} ORDER BY ?wellbore";

		long start = System.nanoTime();
		QuestOWLResultSet res = (QuestOWLResultSet) st.executeTuple(query);
		long end = System.nanoTime();

		double time = (end - start) / 1000000;

		int count = 0;
		while (res.nextRow()) {
			count += 1;
		}
		log.debug("Total result: {}", count);
		log.debug("Query execution time: {} ms", time);

		st.close();
		quest.dispose();
	}

}
