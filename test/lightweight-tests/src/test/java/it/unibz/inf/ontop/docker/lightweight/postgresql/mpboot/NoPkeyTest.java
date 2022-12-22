package it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot;

import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot.utils.MPBootTestsHelper;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.JoinPairs;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.bootconfparser.BootConfParser;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.sqlparser.WorkloadParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

@PostgreSQLLightweightTest
public class NoPkeyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoPkeyTest.class);

    // Reference and to-be--compared files
    private static final String referenceOBDA = "src/test/resources/mpboot/spider_wta_1/reference-spider_wta_1.obda";
    private static final String referenceOWL = "src/test/resources/mpboot/spider_wta_1/reference-spider_wta_1.owl";

    // Queries
    private static final String testQueries = "src/test/resources/mpboot/spider_wta_1/spider_wta_1_workload.json";

    // Bootstrapper Configuration
    private static final String bootConfFile = "src/test/resources/mpboot/spider_wta_1/spider_wta_1_conf.json";

    // DB-connection
    private static final String owlPath = "src/test/resources/mpboot/spider_wta_1/spider_wta_1.owl";
    private static final String obdaPath = "src/test/resources/mpboot/spider_wta_1/spider_wta_1.obda";
    private static final String propertyPath = "/mpboot/spider_wta_1/spider_wta_1.properties";

    // Bootstrapping-info
    private static final String BASE_IRI = "http://semanticweb.org/spider_wta_1/";
    private static final String bootOwlPath = "src/test/resources/mpboot/spider_wta_1/boot-spider_wta_1.owl";
    private static final String bootOBDAPath = "src/test/resources/mpboot/spider_wta_1/boot-spider_wta_1.obda";

    @Test
    public void testWta_1Bootstrapping(){

        LOGGER.debug(new Object(){}.getClass().getEnclosingMethod().getName());

        WorkloadParser parser = new WorkloadParser();

        try {
            BootConf.NullValue nullValue = BootConfParser.parseNullValue(bootConfFile);
            List<String>workload = MPBootTestsHelper.getWorkloadQueries(testQueries);
            JoinPairs pairs = new JoinPairs();
            for( String query : workload ){
                pairs.unite(parser.parseQuery(query)); // Side effect on empty
            }

            OntopSQLOWLAPIConfiguration initialConfiguration = MPBootTestsHelper.configure(propertyPath, owlPath, obdaPath);

            BootConf bootConf = new BootConf.Builder()
                    .joinPairs(pairs)
                    .enableSH(false)
                    .nullValue(nullValue)
                    .build();
            Bootstrapper.BootstrappingResults results = MPBootTestsHelper.bootstrapMapping(initialConfiguration, bootConf, BASE_IRI, MPBootTestsHelper.Method.MPBOOT);

            SQLPPMapping bootstrappedMappings = results.getPPMapping();
            OWLOntology boootstrappedOnto = results.getOntology();

            // Serialize
            MPBootTestsHelper.serializeMappingsAndOnto(bootstrappedMappings, boootstrappedOnto, bootOwlPath, bootOBDAPath);
        } catch (IOException | JSQLParserException | OWLOntologyStorageException | OWLOntologyCreationException |
                 MappingException | MappingBootstrappingException | InvalidQueryException e) {
            e.printStackTrace();
        }

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
    }

}
