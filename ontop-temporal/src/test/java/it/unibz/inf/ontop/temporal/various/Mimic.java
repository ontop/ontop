package it.unibz.inf.ontop.temporal.various;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

public class Mimic {
    @Test
    public void test1(){

        String query = "PREFIX ms:  <http://www.semanticweb.org/ontologies/2018/4/mimic/>\n" +
                "PREFIX icd: <http://purl.bioontology.org/ontology/HOM-ICD9CM/>\n" +
                        "SELECT ?s " +
                        "WHERE {" +
                        "?s a icd:995.91 ." +
                        "}";

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
