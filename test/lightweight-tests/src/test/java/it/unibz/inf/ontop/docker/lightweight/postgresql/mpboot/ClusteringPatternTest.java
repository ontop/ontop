package it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot;

import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot.utils.MPBootTestsHelper;
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

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

@PostgreSQLLightweightTest
public class ClusteringPatternTest {
    // Reference
    private static final String referenceOBDAPath = "src/test/resources/mpboot/clustering/reference-clustering.obda";
    private static final String referenceOWLPath = "src/test/resources/mpboot/clustering/reference-clustering.owl";

    // DB-connection
    private static final String owlPath = "src/test/resources/mpboot/clustering/clustering.owl";
    private static final String obdaPath = "src/test/resources/mpboot/clustering/clustering.obda";
    private static final String propertyPath = "/mpboot/clustering/clustering.properties";

    // Bootstrapping-info
    private static final String BASE_IRI = "http://semanticweb.org/clustering/";
    private static final String bootOwlPath = "src/test/resources/mpboot/clustering/boot-clustering.owl";
    private static final String bootOBDAPath = "src/test/resources/mpboot/clustering/boot-clustering.obda";

    // Bootstrapper conf file
    private static final String CONF_FILE = "src/test/resources/mpboot/clustering/boot-conf.json";

    @Test
    public void testClusteringClasses() { // It also tests the order of arguments

        OntopSQLOWLAPIConfiguration initialConfiguration = MPBootTestsHelper.configure(propertyPath, owlPath, obdaPath);
        try {
            BootConf bootConf = new BootConf.Builder()
                    .clusters(BootConfParser.parseClustering(CONF_FILE))
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
