package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.File;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class NPDUndolferTest extends TestCase {
	
	private final String owlfile = "src/test/resources/npd-v2-ql_a.owl";

	private OWLOntology ontology;
	private OWLOntologyManager manager;


	@Before
    public void setUp() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));
    }
	
	/**
	 * Query 6 from the NPD benchmark 
	 * (this query was an indicator for incorrect variable order in SPAQRL Extend)
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void testNpdQ6() throws Exception {
	
		String query =
"PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
"PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>" +
"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
"PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
"PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>" +
"PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
"SELECT DISTINCT ?wellbore (?length * 0.30 AS ?lenghtM) ?company (?year + 2 AS ?YearB) " +
"WHERE {" +
"  ?wc npdv:coreForWellbore" +
"        [ rdf:type                      npdv:Wellbore ;" +
"          npdv:name                     ?wellbore ;" +
"          npdv:wellboreCompletionYear   ?year ;" +
"          npdv:drillingOperatorCompany  [ npdv:name ?company ] " +
"        ] ." +
"  { ?wc npdv:coresTotalLength ?length } " +
"  " +
"  FILTER(?year >= \"2008\"^^xsd:integer && ?length > 50 " +
"  )" +
"} ORDER BY ?wellbore";

		String rewriting = getRewriting(query);
		assertFalse(rewriting.contains("GTE(company,"));
		assertTrue(rewriting.contains("GTE(year_1,"));
	}

	/**
	 * Davide's query
	 * (indicator for incomplete treatment of data properties in the rewriting)
	 * 
	 * @throws Exception
	 */
	
	@Test
	public void testNpdQD() throws Exception {
		
		String query =
"PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
"PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>" +
"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
"PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
"PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>" +
"PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
"SELECT DISTINCT ?wc " + 
"		   WHERE { " +
"		      ?wc npdv:coreForWellbore [ rdf:type npdv:Wellbore ]. " + 
"		   }";

		String rewriting = getRewriting(query);
	}

	@Test
	public void testNpdPROJ() throws Exception {
		
		String query =
"PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
"PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
"PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>" +
"PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
"SELECT DISTINCT ?name ?awc " + 
"		   WHERE { " +
"		      ?awc npdv:coreForWellbore [ rdf:type npdv:Wellbore; " +
"                                        npdv:wellboreCompletionYear   ?year ;" +
"                                        npdv:name  ?name ]. " + 
"		   }";

		String rewriting = getRewriting(query);
	}
	
	@Test
	public void testDD() throws Exception {
		
		String query =
"PREFIX t: <http://www.w3.org/2001/sw/DataAccess/tests/data/TypePromotion/tP-0#> " +
"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + 
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " + 
"ASK " + 
" WHERE { t:double1 rdf:value ?l . " + 
"         t:double1 rdf:value ?r . " + 
"         FILTER ( datatype(?l + ?r) = xsd:double ) }"; 

		String rewriting = getRewriting(query);
	}
	
	 
	
	@Test
	public void testNpdQ0() throws Exception {
		
		String query =
"PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>" +
"PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>" +
"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
"PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>" +
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
"PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>" +
"PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>" +
"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
"SELECT DISTINCT ?wc " + 
"		   WHERE { " +
"		      ?wc npdv:coreForWellbore [ rdf:type <http://sws.ifi.uio.no/vocab/npd-v2#A%20A>; " +
"                                        rdf:type npdv:B%20B; " +
"                                       npdv:name  \"\\\\\" ]. " + 
"		   }";

		String rewriting = getRewriting(query);
	}
	
	/**
	 * constructs a rewriting
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	
	private String getRewriting(String query) throws Exception {
		
		QuestOWLFactory fac = new QuestOWLFactory();

		QuestPreferences pref = new QuestPreferences();
		pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		pref.setCurrentValueOf(QuestPreferences.REWRITE, QuestConstants.TRUE);

		fac.setPreferenceHolder(pref);

		QuestOWL quest = fac.createReasoner(ontology);
		QuestOWLConnection qconn =  quest.getConnection();
		QuestOWLStatement st = qconn.createStatement();
		
		String rewriting = st.getRewriting(query);
		st.close();
		
		quest.dispose();
		
		return rewriting;
	}

    @After
    public void tearDown() throws Exception {
    	ontology = null;
    	manager = null;
    }	
}
