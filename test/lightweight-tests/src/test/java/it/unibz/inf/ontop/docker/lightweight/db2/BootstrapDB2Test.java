package it.unibz.inf.ontop.docker.lightweight.db2;

import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import it.unibz.inf.ontop.docker.lightweight.DB2LightweightTest;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.DirectMappingBootstrapper;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;

@DB2LightweightTest
public class BootstrapDB2Test extends AbstractDockerRDF4JTest {
    private static final String OBDA_FILE = "/stockexchange/db2/output.obda";
    private static final String OWL_FILE = "/stockexchange/db2/output.owl";
    private static final String PROPERTIES_FILE = "/stockexchange/db2/stockexchange-db2.properties";
    private static final String BASE_IRI = "http://db2-bootstrap-test";

    @AfterAll
    public static void after() {
        release();
    }

    @Test
    public void testBootstrap() throws Exception {
        bootstrap(PROPERTIES_FILE, BASE_IRI, OWL_FILE, OBDA_FILE);
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    private static void bootstrap(String propertyFile, String baseIRI, String outputOwlFile, String outputObdaFile) throws Exception {

        String propertyFilePath = AbstractDockerRDF4JTest.class.getResource(propertyFile).getPath();
        String obdaFilePath = AbstractDockerRDF4JTest.class.getResource(outputObdaFile).getPath();
        String owlFilePath = AbstractDockerRDF4JTest.class.getResource(outputOwlFile).getPath();

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .propertyFile(propertyFilePath)
                .build();

        DirectMappingBootstrapper bootstrapper = DirectMappingBootstrapper.defaultBootstrapper();
        DirectMappingBootstrapper.BootstrappingResults results = bootstrapper.bootstrap(config, baseIRI);

        File obdaFile = new File(obdaFilePath);
        OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
        writer.write(obdaFile, results.getPPMapping());

        File ontologyFile = new File(owlFilePath);
        OWLOntology onto = results.getOntology();
        onto.getOWLOntologyManager().saveOntology(onto, new FileDocumentTarget(ontologyFile));
    }
}
