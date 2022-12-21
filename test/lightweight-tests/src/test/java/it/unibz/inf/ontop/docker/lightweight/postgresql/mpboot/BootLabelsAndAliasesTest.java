package it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot;

import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.MPBootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.bootconfparser.BootConfParser;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.dictionary.Dictionary;
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

@PostgreSQLLightweightTest
public class BootLabelsAndAliasesTest {
    // Reference
    private static final String referenceOBDAPath = "src/test/resources/mpboot.labels/reference-labels.obda";
    private static final String referenceOWLPath = "src/test/resources/mpboot.labels/reference-labels.owl";

    // DB-connection
    private static final String owlPath = "src/test/resources/mpboot.labels/labels.owl";
    private static final String obdaPath = "src/test/resources/mpboot.labels/labels.obda";
    private static final String propertyPath = "/mpboot.labels/labels.properties";

    // Bootstrapping-info
    private static final String BASE_IRI = "http://semanticweb.org/labels/";
    private static final String bootOwlPath = "src/test/resources/mpboot.labels/boot-labels.owl";
    private static final String bootOBDAPath = "src/test/resources/mpboot.labels/boot-labels.obda";

    // Bootstrapper Configuration File
    private static final String CONF_FILE = "src/test/resources/mpboot.labels/boot-conf.json";

    @Test
    public void testLabelsAndAliasesGeneration() {

        OntopSQLOWLAPIConfiguration initialConfiguration = configure();
        try {
            Dictionary dict = BootConfParser.parseDictionary(CONF_FILE);
            boolean enableSH = BootConfParser.parseEnableSH(CONF_FILE);

            BootConf bootConf = new BootConf.Builder()
                    .dictionary(dict)
                    .enableSH(enableSH)
                    .build();

            Bootstrapper.BootstrappingResults results = bootstrapMPMapping(initialConfiguration, bootConf);

            SQLPPMapping bootstrappedMappings = results.getPPMapping();
            OWLOntology boootstrappedOnto = results.getOntology();

            serializeMappingsAndOnto(bootstrappedMappings, boootstrappedOnto);

        } catch (IOException | OWLOntologyCreationException | MappingException | MappingBootstrappingException | OWLOntologyStorageException e) {
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

    private static Bootstrapper.BootstrappingResults bootstrapMPMapping(OntopSQLOWLAPIConfiguration initialConfiguration,
                                                                        BootConf bootConf)

            throws OWLOntologyCreationException, MappingException, MappingBootstrappingException {
        MPBootstrapper bootstrapper = (MPBootstrapper) Bootstrapper.mpBootstrapper();

        // The bootstrappped mappings are appended to those already present in "initialConfiguration"
        Bootstrapper.BootstrappingResults results = bootstrapper.bootstrap(initialConfiguration, BASE_IRI, bootConf);

        return results;
    }

    private static OntopSQLOWLAPIConfiguration configure() {

        String propertyFilePath = BootLabelsAndAliasesTest.class.getResource(propertyPath).getPath();

        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlPath)
                .nativeOntopMappingFile(obdaPath)
                .propertyFile(propertyFilePath)
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
