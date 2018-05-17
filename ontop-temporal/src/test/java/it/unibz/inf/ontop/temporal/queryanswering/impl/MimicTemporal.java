package it.unibz.inf.ontop.temporal.queryanswering.impl;

import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

public class MimicTemporal {
    @Test
    public void test1(){

        String query =
                "PREFIX mt: <http://www.semanticweb.org/ontologies/2018/4/mimic/temporal/>\n" +
                        "PREFIX ms: <http://www.semanticweb.org/ontologies/2018/4/mimic/>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX time: <http://www.w3.org/2006/time#>" +
                        "SELECT ?p ?bInc ?b ?e ?eInc " +
                        "WHERE {" +
                        "GRAPH ?g {?p rdf:type mt:ICUPatientHavingFever .}" +
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
                    .ontologyFile("src/test/resources/mimic.owl")
                    .nativeOntopMappingFile("src/test/resources/mimic.obda")
                    .nativeOntopTemporalMappingFile("src/test/resources/mimic.tobda")
                    .nativeOntopTemporalRuleFile("src/test/resources/mimic.dmtl")
                    .propertyFile("src/test/resources/mimic.properties")
                    .build();

            factory.createReasoner(configuration).getConnection().createStatement().executeSelectQuery(query);

        } catch (OWLException e) {
            e.printStackTrace();
        }
    }

}
