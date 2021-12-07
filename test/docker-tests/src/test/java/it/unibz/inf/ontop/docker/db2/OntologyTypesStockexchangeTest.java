package it.unibz.inf.ontop.docker.db2;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Test if the datatypes are assigned correctly.
 * 
 * NOTE: xsd:string and rdfs:Literal are different.
 * 
 */

public class OntologyTypesStockexchangeTest extends AbstractVirtualModeTest {
    
	static final String owlFile = "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	static final String obdaFile = "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-db2.obda";
    static final String propertyFile = "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-db2.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertyFile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }


    //we need xsd:string to work correctly
    @Test
	public void testQuotedLiteral() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?street WHERE {?x a :Address; :inStreet ?street; :inCity \"Bolzano\".}";

		countResults(2, query1);
	}

    @Test
    public void testDatatypeString() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?street WHERE {?x a :Address; :inStreet ?street; :inCity \"Bolzano\"^^xsd:string .}";

        countResults(2, query1);
    }

    //we need xsd:string to work correctly
    @Test
    public void testAddressesQuotedLiteral() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT * WHERE {      \n" +
                "\t    $x a :Address . \n" +
                "\t\t$x :addressID $id. \n" +
                "\t\t$x :inStreet \"Via Marconi\". \n" +
                "\t\t$x :inCity \"Bolzano\". \n" +
                "\t\t$x :inCountry $country. \n" +
                "\t\t$x :inState $state. \n" +
                "\t\t$x :hasNumber $number.\n" +
                "}";

        countResults(1, query1);
    }

    //in db2 there is no boolean type we refer to it in the database with a smallint 1 for true and a smallint 0 for false
    @Test
    public void testBooleanDatatype() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares \"1\"^^xsd:integer . }";

        countResults(5, query1);
    }

    @Test
    public void testBooleanInteger() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares 1 . }";

        countResults(5, query1);
    }

    //in db2 there is no boolean datatype, it is substitute with smallint
    @Test
    public void testBoolean() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares TRUE . }";

        countResults(0, query1);
    }

    //in db2 there is no boolean datatype, it is substitute with smallint
    @Test
    public void testBooleanTrueDatatype() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares \"1\"^^xsd:boolean . }";

        countResults(0, query1);
    }

    @Test
    public void testFilterBoolean() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?amount WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares ?type. FILTER ( ?type = 1 ). }";

        countResults(5, query1);
    }

    @Test
    public void testNotFilterBoolean() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?amount WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares ?type. FILTER ( ?type != 1 ). }";

        countResults(5, query1);
    }
    
    //a quoted integer is treated as a literal
    @Test
    public void testQuotedInteger() throws Exception {

          String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares \"1\" . }";

          countResults(0, query1);
    }


    @Test
    public void testDatetime() throws Exception {

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n " +
                "SELECT DISTINCT ?x " +
                "WHERE { " +
                "  ?x a :Transaction; :transactionID ?id; \n" +
                "  :transactionDate ?d . \n" +
                "  FILTER(?d = \"2008-04-02T00:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                "}";

        countResults(1, query1);
    }

    //a quoted datatype is treated as a literal
    @Test
    public void testQuotedDatatype() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Transaction; :transactionID ?id; :transactionDate \"2008-04-02T00:00:00\" . }";

        countResults(0, query1);
    }

    // The ontology contains three facts (Joe, Jane, Bane are Investors), the database contains three more.
    @Test
    public void testAbox() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Investor. }";
        countResults(6, query1);
    }

    @Ignore("Consider updating the DB2 instance as its TZ behavior does not seem to be compliant with the docs")
    @Test
    public void testDatetimeTimezone() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n " +
                "SELECT DISTINCT ?x WHERE { \n" +
                "  ?x a :Transaction; :transactionID ?id; \n" +
                "  :transactionDate ?d . \n" +
                "  FILTER (?d = \"2008-04-02T00:00:00+06:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";

        countResults(1, query1);
    }
}
