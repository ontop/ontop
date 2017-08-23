package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;

public class LucadaTest {




    @Test
    public void testConversion() throws OWLOntologyCreationException {

        OntopOWLReasoner reasoner = createReasoner();

    }

    private OntopOWLReasoner createReasoner() throws OWLOntologyCreationException {
        File resourceDir = new File("src/test/resources/lucada/");
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(new File(resourceDir, "lucada.obda"))
                .ontologyFile(new File(resourceDir, "lucada.owl"))
                .propertyFile(new File(resourceDir, "lucada.properties"))
                .enableTestMode()
                .build();
        return  factory.createReasoner(config);


    }

}
