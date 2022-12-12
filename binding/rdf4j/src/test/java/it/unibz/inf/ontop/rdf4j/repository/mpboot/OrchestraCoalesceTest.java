package it.unibz.inf.ontop.rdf4j.repository.mpboot;

import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.MPBootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.bootconfparser.BootConfParser;
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

public class OrchestraCoalesceTest {

    // Bootstrapper Configuration
    private static final String bootConfFile = "src/test/resources/bootstrapper.spider_orchestra/spider_orchestra_conf.json";

    // DB-connection
    private static final String owlPath = "src/test/resources/bootstrapper.spider_orchestra/spider_orchestra.owl";
    private static final String obdaPath = "src/test/resources/bootstrapper.spider_orchestra/spider_orchestra.obda";
    private static final String propertyPath = "src/test/resources/bootstrapper.spider_orchestra/spider_orchestra.properties";

    // Bootstrapping-info
    private static final String BASE_IRI = "http://semanticweb.org/spider_orchestra/";
    private static final String bootOwlPath = "src/test/resources/bootstrapper.spider_orchestra/boot-spider_orchestra.owl";
    private static final String bootOBDAPath = "src/test/resources/bootstrapper.spider_orchestra/boot-spider_orchestra.obda";

    // Reference
    private final static String referenceOBDA = "src/test/resources/bootstrapper.spider_orchestra/reference-spider_orchestra.obda";
    private final static String referenceOWL = "src/test/resources/bootstrapper.spider_orchestra/reference-spider_orchestra.owl";

    @Test
    public void testOrchestraBootstrapping(){

        try {
            BootConf.NullValue nullValue = BootConfParser.parseNullValue(bootConfFile);

            OntopSQLOWLAPIConfiguration initialConfiguration = configureOntop();
            BootConf bootConf = new BootConf.Builder()
                    .enableSH(false)
                    .nullValue(nullValue)
                    .build();
            Bootstrapper.BootstrappingResults results = bootstrapMPMapping(initialConfiguration, bootConf);

            SQLPPMapping bootstrappedMappings = results.getPPMapping();
            OWLOntology boootstrappedOnto = results.getOntology();

            // Serialize
            serializeMappingsAndOnto(bootstrappedMappings, boootstrappedOnto);

            File refOBDAFile = new File(referenceOBDA);
            File refOWLFile = new File(referenceOWL);

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

        } catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException | MappingException | MappingBootstrappingException e) {
            e.printStackTrace();
        }
    }

    private static OntopSQLOWLAPIConfiguration configureOntop() {
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

    private static Bootstrapper.BootstrappingResults bootstrapMPMapping(OntopSQLOWLAPIConfiguration initialConfiguration,
                                                                        BootConf bootConf)

            throws OWLOntologyCreationException, MappingException, MappingBootstrappingException {

        MPBootstrapper bootstrapper = (MPBootstrapper) Bootstrapper.mpBootstrapper();

        // Create configuration here

        // Davide> The bootstrappped mappings are appended to those already in "initialConfiguration"
        Bootstrapper.BootstrappingResults results = bootstrapper.bootstrap(initialConfiguration, BASE_IRI, bootConf);

        return results;
    }
}
