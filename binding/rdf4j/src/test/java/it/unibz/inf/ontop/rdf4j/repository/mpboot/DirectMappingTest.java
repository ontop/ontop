package it.unibz.inf.ontop.rdf4j.repository.mpboot;

import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.DefaultDirectMappingBootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.MPBootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.bootconfparser.BootConfParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class DirectMappingTest {
    // Reference
    private static final String referenceOBDAPath = "src/test/resources/bootstrapper.direct_mapping/reference-direct_mapping.obda";
    private static final String referenceOWLPath = "src/test/resources/bootstrapper.direct_mapping/reference-direct_mapping.owl";

    // DB-connection
    private static final String owlPath = "src/test/resources/bootstrapper.direct_mapping/direct_mapping.owl";
    private static final String obdaPath = "src/test/resources/bootstrapper.direct_mapping/direct_mapping.obda";
    private static final String propertyPath = "src/test/resources/bootstrapper.direct_mapping/direct_mapping.properties";

    // Bootstrapping-info
    private static final String BASE_IRI = "http://semanticweb.org/skyserver/";
    private static final String bootOwlPath = "src/test/resources/bootstrapper.direct_mapping/boot-direct_mapping.owl";
    private static final String bootOBDAPath = "src/test/resources/bootstrapper.direct_mapping/boot-direct_mapping.obda";

    @Test
    public void testDirectMapping() { // It also tests the order of arguments

        OntopSQLOWLAPIConfiguration initialConfiguration = configure();

        BootConf bootConf = new BootConf.Builder().build();

        try {
            Bootstrapper.BootstrappingResults results = bootstrapMapping(initialConfiguration, bootConf);

            SQLPPMapping bootstrappedMappings = results.getPPMapping();
            OWLOntology boootstrappedOnto = results.getOntology();

            serializeMappingsAndOnto(bootstrappedMappings, boootstrappedOnto);
        } catch (OWLOntologyCreationException | MappingException | MappingBootstrappingException | IOException |
                 OWLOntologyStorageException e) {
            e.printStackTrace();
        }

        File refOBDAFile = new File(referenceOBDAPath);
        File refOWLFile = new File(referenceOWLPath);

        File bootOBDAFile = new File(bootOBDAPath);
        File bootOWLFile = new File(bootOwlPath);
        try {
            boolean isOBDAEqual = FileUtils.contentEquals(refOBDAFile, bootOBDAFile);
            boolean isOWLEqual =  FileUtils.contentEquals(refOWLFile, bootOWLFile);
            assertTrue(isOBDAEqual);
            assertTrue(isOWLEqual);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Bootstrapper.BootstrappingResults bootstrapMapping(OntopSQLOWLAPIConfiguration initialConfiguration, BootConf bootConf)
            throws OWLOntologyCreationException, MappingBootstrappingException, MappingException {
        DefaultDirectMappingBootstrapper bootstrapper = Bootstrapper.defaultBootstrapper();

        // Davide> The bootstrappped mappings are appended to those already in "initialConfiguration"
        Bootstrapper.BootstrappingResults results = bootstrapper.bootstrap(initialConfiguration, BASE_IRI, bootConf);

        return results;
    }

    private static OntopSQLOWLAPIConfiguration configure() {
        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlPath)
                .nativeOntopMappingFile(obdaPath)
                .propertyFile(propertyPath)
                .enableTestMode()
                .build();

        return configuration;
    }

    private static void serializeMappingsAndOnto(SQLPPMapping mapping, OWLOntology onto) throws IOException, OWLOntologyStorageException {

        File bootOwlFile = new File(bootOwlPath);
        File bootOBDAFile = new File(bootOBDAPath);

        OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
        writer.write(bootOBDAFile, mapping);

        onto.getOWLOntologyManager().saveOntology(onto, new OWLXMLDocumentFormat(), new FileDocumentTarget(bootOwlFile));
    }

}
