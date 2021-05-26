package it.unibz.inf.ontop.docker.db2;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.spec.mapping.bootstrap.DirectMappingBootstrapper;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import org.junit.Test;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;

import static it.unibz.inf.ontop.spec.mapping.bootstrap.DirectMappingBootstrapper.BootstrappingResults;

public class DB2BootstrapTest {

    static private final String baseIRI = "http://db2-bootstrap-test";
    static private final String owlOutputFile = "src/test/resources/db2/bootstrap/output.owl";
    static private final String obdaOutputFile = "src/test/resources/db2/bootstrap/output.obda";

    private final String propertyFile = this.getClass().getResource("/db2/db2-stock.properties").toString();

    @Test
    public void testBootstrap() throws Exception {
        bootstrap();
        loadGeneratedFiles();
    }

    private void loadGeneratedFiles() {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlOutputFile)
                .nativeOntopMappingFile(obdaOutputFile)
                .propertyFile(propertyFile)
                .enableTestMode()
                .build();
        try {
            OntopOWLReasoner reasoner = factory.createReasoner(config);
            OWLConnection conn = reasoner.getConnection();
            OWLStatement st = conn.createStatement();
        }
        catch (OWLException e) {
            throw new RuntimeException("Error occurred while loading bootstrapped files: " + e);
        }
    }

    private void bootstrap() throws Exception {
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .propertyFile(propertyFile)
                .build();

        DirectMappingBootstrapper bootstrapper = DirectMappingBootstrapper.defaultBootstrapper();
        BootstrappingResults results = bootstrapper.bootstrap(config, baseIRI);

        File obdaFile = new File(obdaOutputFile);
        OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
        writer.write(obdaFile, results.getPPMapping());

        File ontologyFile = new File(owlOutputFile);
        OWLOntology onto = results.getOntology();
        onto.getOWLOntologyManager().saveOntology(onto, new FileDocumentTarget(ontologyFile));
    }
}
