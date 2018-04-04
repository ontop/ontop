package it.unibz.inf.ontop.temporal.queryanswering.impl;

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
    public void test1(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?tb ?bInc ?b ?e ?eInc " +
                        "WHERE {" +
                            "?tb a :Turbine ." +
                            "GRAPH ?g {?tb rdf:type st:PurgingIsOver .}" +
                            "?g time:hasTime _:intv ." +
                            "_:inv time:isBeginInclusive ?bInc ." +
                            "_:intv time:hasBeginning _:beginInst ." +
                            "_:beginInst rdf:type time:Instant ." +
                            "_:beginInst time:inXSDDateTime ?b ." +
                            "_:intv time:hasEnd _:endInst ." +
                            "_:endInst rdf:type time:Instant ." +
                            "_:endInst time:inXSDDateTime ?e ." +
                            "_:inv time:isEndInclusive ?eInc . " +
                        "}";


        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

        OntopTemporalSQLOWLAPIConfiguration configuration = OntopTemporalSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile("src/test/resources/siemens.owl")
                .nativeOntopTemporalMappingFile("src/test/resources/siemens1.tobda")
                .nativeOntopMappingFile("src/test/resources/siemens1.obda")
                .nativeOntopTemporalRuleFile("src/test/resources/rule.dmtl")
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
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

    @Test
    public void test2(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                        "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?tb ?bInc ?b ?e ?eInc " +
                        "WHERE {" +
                        "GRAPH ?g {?tb rdf:type st:PurgingIsOver . ?tb rdf:type st:MainFlameOn .}" +
                        "?g time:hasTime _:intv ." +
                        "_:inv time:isBeginInclusive ?bInc ." +
                        "_:intv time:hasBeginning _:beginInst ." +
                        "_:beginInst rdf:type time:Instant ." +
                        "_:beginInst time:inXSDDateTime ?b ." +
                        "_:intv time:hasEnd _:endInst ." +
                        "_:endInst rdf:type time:Instant ." +
                        "_:endInst time:inXSDDateTime ?e ." +
                        "_:inv time:isEndInclusive ?eInc . " +
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

    @Test
    public void test3(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                        "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?tb ?b ?e ?b2 ?e2 " +
                        "WHERE {" +
                        "GRAPH ?g {?tb rdf:type st:PurgingIsOver .}" +
                        "GRAPH ?g2 {?tb rdf:type st:PurgingIsOver .}" +
                        "?g time:hasTime _:intv ." +
                        "_:intv time:hasBeginning _:beginInst ." +
                        "_:beginInst rdf:type time:Instant ." +
                        "_:beginInst time:inXSDDateTime ?b ." +
                        "_:intv time:hasEnd _:endInst ." +
                        "_:endInst rdf:type time:Instant ." +
                        "_:endInst time:inXSDDateTime ?e ." +
                        "?g2 time:hasTime _:intv2 ." +
                        "_:intv2 time:hasBeginning _:beginInst2 ." +
                        "_:beginInst2 rdf:type time:Instant ." +
                        "_:beginInst2 time:inXSDDateTime ?b2 ." +
                        "_:intv2 time:hasEnd _:endInst2 ." +
                        "_:endInst2 rdf:type time:Instant ." +
                        "_:endInst2 time:inXSDDateTime ?e2 ." +
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

    @Test
    public void test4(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                        "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?tb ?rs " +
                        "WHERE {" +
                        "?tb st:monitoredBySpeedSensor ?rs ." +
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
                .build();

        try {
            reasoner = factory.createReasoner(configuration);
            // Now we are ready for querying
            conn = reasoner.getConnection();
            OWLStatement st = conn.createStatement();
            st.executeSelectQuery(query);

        } catch (OWLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test5(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                        "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?tb ?bInc ?b ?e ?eInc " +
                        "WHERE {" +
                        "GRAPH ?g {?tb rdf:type st:LowRotorSpeedForTenSec .}" +
                        "?g time:hasTime _:intv ." +
                        "_:inv time:isBeginInclusive ?bInc ." +
                        "_:intv time:hasBeginning _:beginInst ." +
                        "_:beginInst rdf:type time:Instant ." +
                        "_:beginInst time:inXSDDateTime ?b ." +
                        "_:intv time:hasEnd _:endInst ." +
                        "_:endInst rdf:type time:Instant ." +
                        "_:endInst time:inXSDDateTime ?e ." +
                        "_:inv time:isEndInclusive ?eInc . " +
                        "}";


        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

        OntopTemporalSQLOWLAPIConfiguration configuration = OntopTemporalSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile("src/test/resources/siemens.owl")
                .nativeOntopTemporalMappingFile("src/test/resources/siemens1.tobda")
                .nativeOntopMappingFile("src/test/resources/siemens1.obda")
                .nativeOntopTemporalRuleFile("src/test/resources/rule.dmtl")
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
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

    @Test
    public void test6(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                        "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?rs " +
                        "WHERE {" +
                        "?rs a :RotationSpeedSensor ." +
                        "}";


        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

        OntopTemporalSQLOWLAPIConfiguration configuration = OntopTemporalSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile("src/test/resources/siemens.owl")
                .nativeOntopTemporalMappingFile("src/test/resources/siemens1.tobda")
                .nativeOntopMappingFile("src/test/resources/siemens1.obda")
                .nativeOntopTemporalRuleFile("src/test/resources/rule.dmtl")
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
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

    @Test
    public void test7(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                        "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?tb ?bInc ?b ?e ?eInc " +
                        "WHERE {" +
                        "GRAPH ?g {?tb rdf:type st:HighAndLowRotorSpeed .}" +
                        "?g time:hasTime _:intv ." +
                        "_:inv time:isBeginInclusive ?bInc ." +
                        "_:intv time:hasBeginning _:beginInst ." +
                        "_:beginInst rdf:type time:Instant ." +
                        "_:beginInst time:inXSDDateTime ?b ." +
                        "_:intv time:hasEnd _:endInst ." +
                        "_:endInst rdf:type time:Instant ." +
                        "_:endInst time:inXSDDateTime ?e ." +
                        "_:inv time:isEndInclusive ?eInc . " +
                        "}";


        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

        OntopTemporalSQLOWLAPIConfiguration configuration = OntopTemporalSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile("src/test/resources/siemens.owl")
                .nativeOntopTemporalMappingFile("src/test/resources/siemens1.tobda")
                .nativeOntopMappingFile("src/test/resources/siemens1.obda")
                .nativeOntopTemporalRuleFile("src/test/resources/rule.dmtl")
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
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
