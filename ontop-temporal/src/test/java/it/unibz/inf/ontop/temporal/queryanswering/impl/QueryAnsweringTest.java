package it.unibz.inf.ontop.temporal.queryanswering.impl;

import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

public class QueryAnsweringTest {

    @Test
    public void test1(){

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

        executeQuery(query);


    }



    @Test
    public void test2(){

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

        executeQuery(query);


    }

    @Test
    public void test3(){

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

        executeQuery(query);
    }

    @Test
    public void test4(){

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                        "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?tb ?rs " +
                        "WHERE {" +
                        "?tb st:monitoredBySpeedSensor ?rs ." +
                        "}";

        executeQuery(query);
    }

    @Test
    public void test5(){

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

        executeQuery(query);


    }

    @Test
    public void test6(){

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                        "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?rs " +
                        "WHERE {" +
                        "?rs a :RotationSpeedSensor ." +
                        "}";

        executeQuery(query);
    }

    @Test
    public void test7(){

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

        executeQuery(query);

    }

    @Test
    public void test8(){

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                        "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?ts ?bInc ?b ?e ?eInc " +
                        "WHERE {" +
                        "GRAPH ?g {?ts rdf:type st:MainFlameOnForTenSec .}" +
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

        executeQuery(query);


    }

    @Test
    public void test9(){

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                        "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?ts ?bInc ?b ?e ?eInc " +
                        "WHERE {" +
                        "GRAPH ?g {?ts rdf:type st:MainFlameOnOfTurbine .}" +
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
        executeQuery(query);


    }

    @Test
    public void test10(){

        String query =
                "PREFIX : <http://siemens.com/ns#>\n" +
                        "PREFIX st:  <http://siemens.com/temporal/ns#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?ts ?bInc ?b ?e ?eInc " +
                        "WHERE {" +
                        "GRAPH ?g {?ts rdf:type st:IncreasingRotorSpeed .}" +
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
        executeQuery(query);


    }

    private void executeQuery(String query) {
        try {
            OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

            OntopTemporalSQLOWLAPIConfiguration configuration = OntopTemporalSQLOWLAPIConfiguration.defaultBuilder()
                    .ontologyFile("src/test/resources/siemens.owl")
                    .nativeOntopTemporalMappingFile("src/test/resources/siemens.tobda")
                    .nativeOntopMappingFile("src/test/resources/siemens.obda")
                    .nativeOntopTemporalRuleFile("src/test/resources/siemens.dmtl")
                    .propertyFile("src/test/resources/siemens.properties")
                    .build();

            factory.createReasoner(configuration).getConnection().createStatement().executeSelectQuery(query);

        } catch (OWLException e) {
            e.printStackTrace();
        }
    }

}
