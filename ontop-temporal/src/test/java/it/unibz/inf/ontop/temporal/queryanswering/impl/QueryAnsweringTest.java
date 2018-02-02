package it.unibz.inf.ontop.temporal.queryanswering.impl;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class QueryAnsweringTest {

    protected OntopOWLReasoner reasoner;
    protected OntopOWLConnection conn;

    @Test
    public void test(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?pio ?b ?e" +
                        "WHERE {" +
                            "GRAPH ?g {?pio rdf:type st:PurgingIsOver .}" +
                            "?g time:hasTime ?intv ." +
                            "?intv time:hasBeginning ?beginInst ." +
                            "?beginInst rdf:type time:Instant ." +
                            "?beginInst time:inXSDDateTime ?b ." +
                            "?intv time:hasEnd ?endInst ." +
                            "?endInst rdf:type time:Instant ." +
                            "?endInst time:inXSDDateTime ?e ." +
                        "}";

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

        OntopTemporalSQLOWLAPIConfiguration configuration = OntopTemporalSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile("src/test/resources/siemens.owl")
                .nativeOntopTemporalMappingFile("src/test/resources/siemens.tobda")
                .nativeOntopMappingFile("src/test/resources/siemens.obda")
                .nativeOntopTemporalRuleFile("src/test/resources/rule.dmtl")
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
                .enableTestMode()
                .build();

        try {
            reasoner = factory.createReasoner(configuration);
            // Now we are ready for querying
            conn = reasoner.getConnection();
            OWLStatement st = conn.createStatement();
            st.executeSelectQuery(query);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        } catch (OWLException e) {
            e.printStackTrace();
        }


    }
}
