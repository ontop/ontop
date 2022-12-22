package it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot;

import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.bootconfparser.BootConfParser;
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

import static it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot.utils.MPBootTestsHelper.*;
import static it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot.utils.MPBootTestsHelper.Method.MPBOOT;
import static org.junit.Assert.assertTrue;

@PostgreSQLLightweightTest
public class OrchestraCoalesceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoPkeyTest.class);

    // Bootstrapper Configuration
    private static final String bootConfFile = "src/test/resources/mpboot/spider_orchestra/spider_orchestra_conf.json";

    // DB-connection
    private static final String owlPath = "src/test/resources/mpboot/spider_orchestra/spider_orchestra.owl";
    private static final String obdaPath = "src/test/resources/mpboot/spider_orchestra/spider_orchestra.obda";
    private static final String propertyPath = "/mpboot/spider_orchestra/spider_orchestra.properties";

    // Bootstrapping-info
    private static final String BASE_IRI = "http://semanticweb.org/spider_orchestra/";
    private static final String bootOwlPath = "src/test/resources/mpboot/spider_orchestra/boot-spider_orchestra.owl";
    private static final String bootOBDAPath = "src/test/resources/mpboot/spider_orchestra/boot-spider_orchestra.obda";

    // Reference
    private final static String referenceOBDA = "src/test/resources/mpboot/spider_orchestra/reference-spider_orchestra.obda";
    private final static String referenceOWL = "src/test/resources/mpboot/spider_orchestra/reference-spider_orchestra.owl";

    @Test
    public void testOrchestraBootstrapping(){

        LOGGER.error(new Object(){}.getClass().getEnclosingMethod().getName());

        try {
            BootConf.NullValue nullValue = BootConfParser.parseNullValue(bootConfFile);

            OntopSQLOWLAPIConfiguration initialConfiguration = configure(propertyPath, owlPath, obdaPath);
            BootConf bootConf = new BootConf.Builder()
                    .enableSH(false)
                    .nullValue(nullValue)
                    .build();

            Bootstrapper.BootstrappingResults results = bootstrapMapping(initialConfiguration, bootConf, BASE_IRI, MPBOOT);

            SQLPPMapping bootstrappedMappings = results.getPPMapping();
            OWLOntology boootstrappedOnto = results.getOntology();

            // Serialize
            serializeMappingsAndOnto(bootstrappedMappings, boootstrappedOnto, bootOwlPath, bootOBDAPath);

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
}
