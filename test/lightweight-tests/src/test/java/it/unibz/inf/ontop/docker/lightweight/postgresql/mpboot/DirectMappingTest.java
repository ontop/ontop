package it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot;

import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot.utils.MPBootTestsHelper;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
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
public class DirectMappingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectMappingTest.class);
    // Reference
    private static final String referenceOBDAPath = "src/test/resources/mpboot/direct_mapping/reference-direct_mapping.obda";
    private static final String referenceOWLPath = "src/test/resources/mpboot/direct_mapping/reference-direct_mapping.owl";

    // DB-connection
    private static final String owlPath = "src/test/resources/mpboot/direct_mapping/direct_mapping.owl";
    private static final String obdaPath = "src/test/resources/mpboot/direct_mapping/direct_mapping.obda";
    private static final String propertyPath = "/mpboot/direct_mapping/direct_mapping.properties";

    // Bootstrapping-info
    private static final String BASE_IRI = "http://semanticweb.org/skyserver/";
    private static final String bootOwlPath = "src/test/resources/mpboot/direct_mapping/boot-direct_mapping.owl";
    private static final String bootOBDAPath = "src/test/resources/mpboot/direct_mapping/boot-direct_mapping.obda";

    @Test
    public void testDirectMapping() { // It also tests the order of arguments

        LOGGER.debug(new Object(){}.getClass().getEnclosingMethod().getName());
        OntopSQLOWLAPIConfiguration initialConfiguration = MPBootTestsHelper.configure(propertyPath, owlPath, obdaPath);
        BootConf bootConf = new BootConf.Builder().build();

        try {
            Bootstrapper.BootstrappingResults results = MPBootTestsHelper.bootstrapMapping(initialConfiguration, bootConf, BASE_IRI, MPBootTestsHelper.Method.DIRECT);

            SQLPPMapping bootstrappedMappings = results.getPPMapping();
            OWLOntology boootstrappedOnto = results.getOntology();

            MPBootTestsHelper.serializeMappingsAndOnto(bootstrappedMappings, boootstrappedOnto, bootOwlPath, bootOBDAPath);
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
}
