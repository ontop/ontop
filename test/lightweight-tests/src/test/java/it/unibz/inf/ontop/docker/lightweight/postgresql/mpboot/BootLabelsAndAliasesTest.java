package it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot;

import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot.utils.MPBootTestsHelper;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.bootconfparser.BootConfParser;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.dictionary.Dictionary;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

@PostgreSQLLightweightTest
public class BootLabelsAndAliasesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootLabelsAndAliasesTest.class);

    // Reference
    private static final String referenceOBDAPath = "src/test/resources/mpboot/labels/reference-labels.obda";
    private static final String referenceOWLPath = "src/test/resources/mpboot/labels/reference-labels.owl";

    // DB-connection
    private static final String owlPath = "src/test/resources/mpboot/labels/labels.owl";
    private static final String obdaPath = "src/test/resources/mpboot/labels/labels.obda";
    private static final String propertyPath = "/mpboot/labels/labels.properties";

    // Bootstrapping-info
    protected String BASE_IRI = "http://semanticweb.org/labels/";
    private static final String bootOwlPath = "src/test/resources/mpboot/labels/boot-labels.owl";
    private static final String bootOBDAPath = "src/test/resources/mpboot/labels/boot-labels.obda";

    // Bootstrapper Configuration File
    private static final String CONF_FILE = "src/test/resources/mpboot/labels/boot-conf.json";

    @Test
    public void testLabelsAndAliasesGeneration() {

        LOGGER.error(new Object(){}.getClass().getEnclosingMethod().getName());

        OntopSQLOWLAPIConfiguration initialConfiguration = MPBootTestsHelper.configure(propertyPath, owlPath, obdaPath);
        try {
            Dictionary dict = BootConfParser.parseDictionary(CONF_FILE);
            boolean enableSH = BootConfParser.parseEnableSH(CONF_FILE);

            BootConf bootConf = new BootConf.Builder()
                    .dictionary(dict)
                    .enableSH(enableSH)
                    .build();

            Bootstrapper.BootstrappingResults results = MPBootTestsHelper.bootstrapMapping(initialConfiguration, bootConf, BASE_IRI, MPBootTestsHelper.Method.MPBOOT);

            SQLPPMapping bootstrappedMappings = results.getPPMapping();
            OWLOntology boootstrappedOnto = results.getOntology();

            MPBootTestsHelper.serializeMappingsAndOnto(bootstrappedMappings, boootstrappedOnto, bootOwlPath, bootOBDAPath);

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

}
