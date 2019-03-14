package it.unibz.inf.ontop.temporal.various;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

public class Mimic {
    @Test
    public void test1(){

        String query = "PREFIX mt: <http://www.semanticweb.org/ontologies/2018/4/mimic/temporal/>\n" +
                "PREFIX ms: <http://www.semanticweb.org/ontologies/2018/4/mimic/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX time: <http://www.w3.org/2006/time#>\n" +
                "PREFIX HOM-ICD9CM: <http://purl.bioontology.org/ontology/HOM-ICD9CM/>\n" +
                "SELECT ?p ?v ?bInc ?b ?e ?eInc\n" +
                "WHERE {\n" +
                "GRAPH ?g {?p mt:hasFirstDayCreatinineLevel ?v.}\n" +
                "?g time:hasTime _:intv .\n" +
                "_:inv time:isBeginInclusive ?bInc .\n" +
                "_:intv time:hasBeginning _:beginInst .\n" +
                "_:beginInst rdf:type time:Instant .\n" +
                "_:beginInst time:inXSDDateTime ?b .\n" +
                "_:intv time:hasEnd _:endInst .\n" +
                "_:endInst rdf:type time:Instant .\n" +
                "_:endInst time:inXSDDateTime ?e .\n" +
                "_:inv time:isEndInclusive ?eInc .\n" +
                "?p ms:diagnosedWith ?d.\n" +
                "?d a HOM-ICD9CM:MM_CLASS_21613 .";

//        String query = "PREFIX ms:  <http://www.semanticweb.org/ontologies/2018/4/mimic/>\n" +
//                "PREFIX icd: <http://purl.bioontology.org/ontology/ICD9CM/>\n" +
//                "SELECT ?y ?l " +
//                "WHERE {" +
//                "?y rdfs:label ?l ." +
//                "}";

        executeQuery(query);

    }

    private void executeQuery(String query) {
        try {
            OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

            OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                    .ontologyFile("src/test/resources/mimic.owl")
                    .nativeOntopMappingFile("src/test/resources/mimic.obda")
                    .propertyFile("src/test/resources/mimic.properties")
                    .build();

            factory.createReasoner(configuration).getConnection().createStatement().executeSelectQuery(query);

        } catch (OWLException e) {
            e.printStackTrace();
        }
    }
}
