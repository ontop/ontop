package org.semanticweb.ontop.sesame.tests.general;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.Binding;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.ontop.sesame.SesameVirtualRepo;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;



import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

public class AggregatesTestWP5 extends TestCase {

	private OBDADataFactory fac;

	//Logger log = LoggerFactory.getLogger(this.getClass());
	
	private OBDAModel obdaModel;
	private OWLOntology owlontology;
	Model mappings;
	QuestPreferences pref;
	RepositoryConnection con;

	
	final String owlfile = "src/test/resources/wp5/optique-demo.ttl";
	final String mappingfile = "src/test/resources/wp5/mapping.ttl";
	
	
	@Override
	public void setUp() throws Exception {
	
		
	// create owlontology from file
	OWLOntologyManager man = OWLManager.createOWLOntologyManager();
	OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(	new File(owlfile).getParentFile(), false);
	man.addIRIMapper(iriMapper);
	try{
		owlontology = man
				.loadOntologyFromOntologyDocument(new File(owlfile));
	} catch (Exception e) {

		e.printStackTrace();
		assertFalse(false);
	}
		
	/*
	 * Mappings
	 */
	try {
		// create RDF Graph from ttl file
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		InputStream in = new FileInputStream(mappingfile);
		URL documentUrl = new URL("file://" + mappingfile);
		mappings = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(mappings);
		parser.setRDFHandler(collector);
		parser.parse(in, documentUrl.toString());

	} catch (Exception e) {

		e.printStackTrace();
		assertFalse(true);
	}
		
	/*
	 * PREFERENCES
	 */
		
	pref = new QuestPreferences();
	pref.setCurrentValueOf(QuestPreferences.ABOX_MODE,	QuestConstants.VIRTUAL);
//	pref.setCurrentValueOf(QuestPreferences.REWRITE, "true");
//	pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
	// set jdbc params in config
	pref.setCurrentValueOf(QuestPreferences.DBNAME, "siemens-test");
	pref.setCurrentValueOf(QuestPreferences.JDBC_URL,"jdbc:postgresql://10.254.11.17:5432/siemens-test");
	pref.setCurrentValueOf(QuestPreferences.DBUSER, "postgres");
	pref.setCurrentValueOf(QuestPreferences.DBPASSWORD, "postgres");
	pref.setCurrentValueOf(QuestPreferences.JDBC_DRIVER,"org.postgresql.Driver");	
		
	Repository repo;
	try {
		repo = new SesameVirtualRepo("virtualExample2", owlontology, mappings, pref);
		/*
		 * Repository must be always initialized first
		 */
		repo.initialize();

		/*
		 * Get the repository connection
		 */
		con = repo.getConnection();

	} catch (Exception e) {
		e.printStackTrace();
		assertFalse(true);
	}
		
	}

	@Override
	public void tearDown() throws Exception {
		try {
		//	dropTables();
			con.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void executeAndCheckCount(String query, int expectedCount) throws Exception {
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	query);
			TupleQueryResult result = tupleQuery.evaluate();
            int count = 0;

			while (result.hasNext()) {
				for (Binding binding : result.next()) {
					System.out.print(binding.getValue() + ", ");
				}
				System.out.println();
                count++;
			}
			result.close();
            assertEquals(expectedCount, count);

		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}

    public void executeAndCheckOrder(String query, List<Map<String, String>> expectedResults) throws Exception {
        try {
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	query);
            TupleQueryResult result = tupleQuery.evaluate();
            int i = 0;

            while (result.hasNext()) {
                Map<String, String> expectedBindings = expectedResults.get(i);

                for (Binding binding : result.next()) {
                    System.out.print(binding.getValue() + ", ");
                    if (expectedBindings.containsKey(binding.getName())) {
                        assertEquals(expectedBindings.get(binding.getName()), binding.getValue().toString());
                    }
                }
                System.out.println();
                i++;
            }
            result.close();

        } catch (Exception e) {
            e.printStackTrace();
            assertFalse(true);
        }
    }

	
	
	private void runAndCheckCount(String query, int expectedCount) throws Exception {
		try {
			executeAndCheckCount(query, expectedCount);
		} catch (Exception e) {
			assertFalse(true);
			throw e;
		} finally {
			con.close();
		
		}
	}

    private void runAndCheckOrder(String query, List<Map<String, String>> expectedResults) throws Exception {
        try {
            executeAndCheckOrder(query, expectedResults);
        } catch (Exception e) {
            assertFalse(true);
            throw e;
        } finally {
            con.close();

        }
    }



	/*
	 * 	test queries
	 */

	
	
	
	public void testAggrCount1() throws Exception {
		String query = "select ?x (count(?state) as ?c) where { ?x <http://www.siemens.com/demo#hasState> ?state.} GROUP BY ?x LIMIT 1 ";

		runAndCheckCount(query, 1);

	}
	
	public void testMap() throws Exception {
		String query = "PREFIX siemens: <http://www.siemens.com/demo#> " +
                "select *  WHERE { ?measurement a siemens:Measurement ; " +
									"siemens:measuredBy <http://www.optique-project.eu/resource/sensor/Sensor-46> ; " +
									"  siemens:measurementHasTS ?ts;" +
									"   siemens:hasValue ?value. " +
									"   FILTER(?ts > \"2009-01-05T00:00:00.000\"^^xsd:dateTime " +
                "                              && ?ts < \"2009-01-07T15:00:00.000\"^^xsd:dateTime)}  ";

		runAndCheckCount(query, 62);

	}
	
	
	
   public void testAggrCount4() throws Exception {

	   String query = "select ?ts ( COUNT (?et) AS ?c)  WHERE {\n" +
			   "?message a <http://www.siemens.com/demo#Message>;\n" +
			   "   <http://www.siemens.com/demo#messageHasTS> ?ts;\n" +
			   "   <http://www.siemens.com/demo#hasEventtext> ?et.\n" +
			   "   FILTER(?ts > \"2009-01-01T00:00:00Z\"^^xsd:dateTime && ?ts < \"2009-01-07T23:59:59Z\"^^xsd:dateTime)\n" +
			   "}\n" +
			   "GROUP BY ?ts\n" +
			   "LIMIT 20";

	   runAndCheckCount(query, 20);

   }

    public void testAggrCount5() throws Exception {

        String query = "PREFIX siemens: <http://www.siemens.com/demo#>" +
                "SELECT ?assembly (COUNT (?message) AS ?eventFrequency) WHERE { \n" +
                "   ?message a siemens:Message ;\n" +
                "      siemens:forAssembly ?assembly;\n" +
                "      siemens:messageHasTS ?ts .\n" +
                "   FILTER(?ts > \"2009-01-01T00:00:00Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> " +
                "          && ?ts < \"2009-01-03T23:00:00Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)\n" +
                "}\n" +
                "GROUP BY ?assembly";

        runAndCheckCount(query, 10);

    }

    /**
     * TODO: determine prefix ":".
     */
    public void testQS1() throws  Exception {
//        String query
//                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
//                + "Select *  WHERE { " +
//                "?measurement a siemens:Measurement;" +
//                "siemens:measuredBy <http://www.siemens.com/optique/demo/sensor/Sensor57>;" +
//                "siemens:hasTS ?ts;" +
//                "siemens:hasValue ?value." +
//                "?timeSelection :timeSelectionStart ?start ." +
//                "?timeSelection :timeSelectionEnd ?end ." +
//                "FILTER(?ts > xsd:dateTime(?start) && ?ts < xsd:dateTime(?end))" +
//                "}limit 2000";
        String query
                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
                + "Select *  WHERE { " +
                "?measurement a siemens:Measurement; " +
                "siemens:measuredBy <http://www.optique-project.eu/resource/sensor/Sensor-46>; " +
                "siemens:measurementHasTS ?ts; " +
                "siemens:hasValue ?value. " +
                "FILTER(?ts > '2005-01-01T00:00:00Z'^^xsd:dateTime && ?ts < '2005-01-10T23:00:00Z'^^xsd:dateTime)" +
                "}" +
                "limit 200";
        runAndCheckCount(query, 200);
    }

    public void testQS2() throws  Exception {
        String query
                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
                + "SELECT ?eventtext ( COUNT (?eventtext) AS ?count) WHERE { ?message a siemens:Message ;\n" +
                "siemens:messageHasTS ?ts;\n" +
                "siemens:hasEventtext ?eventtext . \n" +
                "   FILTER(?ts > '2009-01-01T00:00:00Z'^^xsd:dateTime && ?ts < '2009-01-03T23:00:00Z'^^xsd:dateTime )\n" +
                "}\n" +
                "GROUP BY ?eventtext ORDER BY DESC(?count) " +
                "LIMIT 5";

        String countKey = "count";
        String countTemplate = "\"%d\"^^<http://www.w3.org/2001/XMLSchema#integer>";

        List<Map<String, String> > expectedResults = new ArrayList<>();
        Map<String, String> result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 99));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 60));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 54));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 40));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 38));
        expectedResults.add(result);

        runAndCheckOrder(query, expectedResults);
    }

    public void testQS3() throws  Exception {
        String query
                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
                + "SELECT ?eventtext ( COUNT (?eventtext) AS ?count) WHERE { ?message a siemens:Message ;\n" +
                "siemens:messageHasTS ?ts;\n" +
                "siemens:hasEventtext ?eventtext . \n" +
                "   FILTER(?ts > '2009-01-01T00:00:00Z'^^xsd:dateTime && ?ts < '2009-01-03T23:00:00Z'^^xsd:dateTime)\n" +
                "}\n" +
                "GROUP BY ?eventtext ORDER BY DESC(?count) LIMIT 10";

        String countKey = "count";
        String countTemplate = "\"%d\"^^<http://www.w3.org/2001/XMLSchema#integer>";

        List<Map<String, String> > expectedResults = new ArrayList<>();
        Map<String, String> result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 99));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 60));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 54));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 40));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 38));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 38));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 36));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 36));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 32));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(countKey, String.format(countTemplate, 30));
        expectedResults.add(result);


        runAndCheckOrder(query, expectedResults);
    }

    public void testQS4() throws Exception {
        String query
                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
                + "SELECT ?assembly (COUNT (?message) AS ?eventFrequency) WHERE { \n"
                + "   ?message a siemens:Message ;\n"
                + "      siemens:hasCategory <http://www.optique-project.eu/resource/event-category/EventCategory-28>;\n"
                + "      siemens:forAssembly ?assembly;\n"
                + "      siemens:messageHasTS ?ts .\n"
                + "   FILTER(?ts > '2005-01-01T00:00:00Z'^^xsd:dateTime && ?ts < '2012-01-03T23:00:00Z'^^xsd:dateTime)\n"
                + "}\n"
                + "GROUP BY ?assembly\n"
                + "ORDER BY ?eventFrequency\n"
                + "LIMIT 4";


        String assemblyPrefix = "http://www.optique-project.eu/resource/assembly/Assembly-";

        String assemblyKey = "assembly";
        String eventFrequencyKey = "eventFrequency";
        String eventFrequencyTemplate = "\"%d\"^^<http://www.w3.org/2001/XMLSchema#integer>";

        List<Map<String, String> > expectedResults = new ArrayList<>();
        Map<String, String> result = new HashMap<>();
        result.put(assemblyKey, assemblyPrefix + "5");
        result.put(eventFrequencyKey, String.format(eventFrequencyTemplate, 20384));
        expectedResults.add(result);

        result = new HashMap<>();
        result.put(assemblyKey, assemblyPrefix + "6");
        result.put(eventFrequencyKey, String.format(eventFrequencyTemplate, 23211));
        expectedResults.add(result);

        result = new HashMap<>();
        result.put(assemblyKey, assemblyPrefix + "8");
        result.put(eventFrequencyKey, String.format(eventFrequencyTemplate, 38195));
        expectedResults.add(result);

        result = new HashMap<>();
        result.put(assemblyKey, assemblyPrefix + "7");
        result.put(eventFrequencyKey, String.format(eventFrequencyTemplate, 38840));
        expectedResults.add(result);

        runAndCheckOrder(query, expectedResults);
    }

    public void testQS5() throws  Exception {
        String query
                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
                + "SELECT ?category (COUNT (?category) AS ?categoryFrequency) WHERE { \n" +
                "?message a siemens:Message ;\n" +
                "siemens:hasCategory ?category;\n" +
                "siemens:forAssembly <http://www.optique-project.eu/resource/assembly/Assembly-6>;\n" +
                "siemens:messageHasTS ?ts .\n" +
                "FILTER(?ts > '2005-03-25T00:00:00Z'^^xsd:dateTime && ?ts < '2006-04-04T00:00:00Z'^^xsd:dateTime)\n" +
                "}\n" +
                "GROUP BY ?category\n" +
                "ORDER BY DESC(?categoryFrequency)\n" +
                "LIMIT 10\n";

        String categoryFrequencyKey = "categoryFrequency";
        String categoryFrequencyTemplate = "\"%d\"^^<http://www.w3.org/2001/XMLSchema#integer>";
        List<Map<String, String> > expectedResults = new ArrayList<>();

        Map<String, String> result = new HashMap<>();
        result.put(categoryFrequencyKey, String.format(categoryFrequencyTemplate, 6718));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(categoryFrequencyKey, String.format(categoryFrequencyTemplate, 5240));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(categoryFrequencyKey, String.format(categoryFrequencyTemplate, 2159));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(categoryFrequencyKey, String.format(categoryFrequencyTemplate, 2106));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(categoryFrequencyKey, String.format(categoryFrequencyTemplate, 1301));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(categoryFrequencyKey, String.format(categoryFrequencyTemplate, 711));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(categoryFrequencyKey, String.format(categoryFrequencyTemplate, 368));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(categoryFrequencyKey, String.format(categoryFrequencyTemplate, 154));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(categoryFrequencyKey, String.format(categoryFrequencyTemplate, 60));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(categoryFrequencyKey, String.format(categoryFrequencyTemplate, 22));
        expectedResults.add(result);

        runAndCheckOrder(query, expectedResults);
    }

    public void testQS6() throws  Exception {
        String query
                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
                + "SELECT ?ts ?eventtext ?category ?turbine WHERE { \n" +
                "   ?event a siemens:Message ; \n" +
                "   siemens:hasEventtext \"Controller fault\"^^xsd:string ;\n" +
                "   siemens:hasEventtext ?eventtext ;\n" +
                "   siemens:forAssembly <http://www.optique-project.eu/resource/assembly/Assembly-6> ;\n" +
                "   siemens:forAssembly ?turbine ;\n" +
                "   siemens:hasCategory ?category ;\n" +
                "   siemens:messageHasTS ?ts .\n" +
                "   FILTER(?ts > '2007-11-15T00:00:00Z'^^xsd:dateTime && ?ts < '2007-12-31T23:59:59Z'^^xsd:dateTime)\n" +
                "}\n" +
                "ORDER BY ASC(?ts)\n" +
                "LIMIT 1\n";

        List<Map<String, String> > expectedResults = new ArrayList<>();
        Map<String, String> result = new HashMap<>();
        result.put("ts", "\"2007-11-15T12:01:50.0\"^^<http://www.w3.org/2001/XMLSchema#dateTime>");
        result.put("eventtext","\"Controller fault\"^^<http://www.w3.org/2001/XMLSchema#string>");
        result.put("category","http://www.optique-project.eu/resource/event-category/EventCategory-31");
        result.put("turbine","http://www.optique-project.eu/resource/assembly/Assembly-6");

        expectedResults.add(result);

        runAndCheckOrder(query, expectedResults);
    }

    public void testQS7() throws  Exception {
        String query
                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
                + "SELECT ?dataset ?ts ?value WHERE { { \n" +
                "   ?measurement a siemens:Measurement ;\n" +
                "   siemens:measuredBy ?dataset ;\n" +
                "   siemens:hasValue ?value ;\n" +
                "   siemens:measurementHasTS ?ts .\n" +
                "   FILTER(?ts > '2005-01-01T00:00:00Z'^^xsd:dateTime && ?ts < '2005-01-01T01:00:00Z'^^xsd:dateTime)\n" +
                "}\n" +
                "UNION\n" +
                "{ \n" +
                "   ?message a siemens:Message ;\n" +
                "   siemens:hasEventtext \"Event452\"^^xsd:string ;\n" +
                "   siemens:hasValue ?value ;\n" +
                "   siemens:messageHasTS ?ts .\n" +
                "   FILTER(?ts = '2005-01-01T00:31:34Z'^^xsd:dateTime)\n" +
                "} }";
        runAndCheckCount(query, 175);
    }

    public void testQS8() throws  Exception {
        String query
                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
                + "SELECT ?dataset ?ts ?value WHERE { { \n" +
                "   ?measurement a siemens:Measurement ;\n" +
                "   siemens:measuredBy ?dataset ;\n" +
                "   siemens:hasValue ?value ;\n" +
                "   siemens:measurementHasTS ?ts .\n" +
                "   FILTER(?ts > '2005-01-01T00:00:00Z'^^xsd:dateTime && ?ts < '2005-01-01T00:31:34Z'^^xsd:dateTime)\n" +
                "}\n" +
                "UNION\n" +
                "{ \n" +
                "   ?message a siemens:Message ;\n" +
                "   siemens:hasEventtext \"Event452\"^^xsd:string ;\n" +
                "   siemens:hasValue ?value ;\n" +
                "   siemens:messageHasTS ?ts .\n" +
                "   FILTER(?ts = '2005-01-01T00:31:34Z'^^xsd:dateTime)\n" +
                "} } \n";
        runAndCheckCount(query, 99);
    }

    // Disabled because prefix : is unknown.
//    public void testQS9() throws  Exception {
//        String query
//                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
//                + "SELECT ?ts ( COUNT (\"Shutdown\") AS ?count) WHERE { ?message a siemens:Message ;\n" +
//                "siemens:messageHasTS ?ts;\n" +
//                "siemens:hasEventtext \"Shutdown\" . \n" +
//                "?? :datetimeSelectionStart ?start .\n" +
//                "?? :datetimeSelectionEnd ?end .\n" +
//                "   FILTER(?ts > xsd:dateTime(?start) && ?ts < xsd:dateTime(?end))\n" +
//                "}\n" +
//                "GROUP BY ?ts LIMIT 20";
//        runAndCheckCount(query, 10);
//    }

    public void testQU1() throws Exception {
        String query
                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
                + "SELECT ?eventtext (COUNT (?eventtext) AS ?freq) WHERE { \n" +
                "   ?message a siemens:Message ;\n" +
                "   siemens:hasEventtext ?eventtext ;\n" +
                "   siemens:forAssembly <http://www.optique-project.eu/resource/assembly/Assembly-6> ;\n" +
                "   siemens:messageHasTS ?ts .\n" +
                "   FILTER(?ts > '2005-01-01T00:00:00Z'^^xsd:dateTime && ?ts <= '2005-01-01T01:44:47Z'^^xsd:dateTime)\n" +
                "} \n" +
                "GROUP BY ?eventtext\n" +
                "ORDER BY DESC(?freq)\n" +
                "LIMIT 10";

        String eventTextKey = "eventtext";
        String textTemplate = "\"%s\"^^<http://www.w3.org/2001/XMLSchema#string>";
        String freqKey = "freq";
        String freqTemplate = "\"%d\"^^<http://www.w3.org/2001/XMLSchema#integer>";
        List<Map<String, String> > expectedResults = new ArrayList<>();

        Map<String, String> result = new HashMap<>();
        result.put(eventTextKey, String.format(textTemplate, "Start initiated"));
        result.put(freqKey, String.format(freqTemplate, 6));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(eventTextKey, String.format(textTemplate, "Event224"));
        result.put(freqKey, String.format(freqTemplate, 5));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(eventTextKey, String.format(textTemplate, "Event14"));
        result.put(freqKey, String.format(freqTemplate, 4));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(eventTextKey, String.format(textTemplate, "Event452"));
        result.put(freqKey, String.format(freqTemplate, 4));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(eventTextKey, String.format(textTemplate, "Event1807"));
        result.put(freqKey, String.format(freqTemplate, 4));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(eventTextKey, String.format(textTemplate, "Event369"));
        result.put(freqKey, String.format(freqTemplate, 4));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(eventTextKey, String.format(textTemplate, "Event1817"));
        result.put(freqKey, String.format(freqTemplate, 3));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(eventTextKey, String.format(textTemplate, "Event1463"));
        result.put(freqKey, String.format(freqTemplate, 3));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(eventTextKey, String.format(textTemplate, "Shutdown"));
        result.put(freqKey, String.format(freqTemplate, 3));
        expectedResults.add(result);
        result = new HashMap<>();
        result.put(eventTextKey, String.format(textTemplate, "Vibration fault"));
        result.put(freqKey, String.format(freqTemplate, 3));
        expectedResults.add(result);

        runAndCheckOrder(query, expectedResults);
    }
}
