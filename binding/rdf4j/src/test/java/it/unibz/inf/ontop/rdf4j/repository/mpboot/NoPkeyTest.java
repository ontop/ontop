package it.unibz.inf.ontop.rdf4j.repository.mpboot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.mpboot.testutils.WorkloadJsonEntry;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.MPBootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.JoinPairs;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.bootconfparser.BootConfParser;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.sqlparser.WorkloadParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class NoPkeyTest {

    // Reference and to-be--compared files
    private static final String referenceOBDA = "src/test/resources/bootstrapper.spider_wta_1/reference-spider_wta_1.obda";
    private static final String referenceOWL = "src/test/resources/bootstrapper.spider_wta_1/reference-spider_wta_1.owl";

    // Queries
    private static final String testQueries = "src/test/resources/bootstrapper.spider_wta_1/spider_wta_1_workload.json";

    // Bootstrapper Configuration
    private static final String bootConfFile = "src/test/resources/bootstrapper.spider_wta_1/spider_wta_1_conf.json";

    // DB-connection
    private static final String owlPath = "src/test/resources/bootstrapper.spider_wta_1/spider_wta_1.owl";
    private static final String obdaPath = "src/test/resources/bootstrapper.spider_wta_1/spider_wta_1.obda";
    private static final String propertyPath = "src/test/resources/bootstrapper.spider_wta_1/spider_wta_1.properties";

    // Bootstrapping-info
    private static final String BASE_IRI = "http://semanticweb.org/spider_wta_1/";
    private static final String bootOwlPath = "src/test/resources/bootstrapper.spider_wta_1/boot-spider_wta_1.owl";
    private static final String bootOBDAPath = "src/test/resources/bootstrapper.spider_wta_1/boot-spider_wta_1.obda";

    @Test
    public void testWta_1Bootstrapping(){
        WorkloadParser parser = new WorkloadParser();

        try {
            BootConf.NullValue nullValue = BootConfParser.parseNullValue(bootConfFile);
            List<String>workload = getWorkloadQueries(testQueries);
            JoinPairs pairs = new JoinPairs();
            for( String query : workload ){
                pairs.unite(parser.parseQuery(query)); // Side effect on empty
            }

            OntopSQLOWLAPIConfiguration initialConfiguration = configureOntop();
            BootConf bootConf = new BootConf.Builder()
                    .joinPairs(pairs)
                    .enableSH(false)
                    .nullValue(nullValue)
                    .build();
            Bootstrapper.BootstrappingResults results = bootstrapMPMapping(initialConfiguration, bootConf);

            SQLPPMapping bootstrappedMappings = results.getPPMapping();
            OWLOntology boootstrappedOnto = results.getOntology();

            // Serialize
            serializeMappingsAndOnto(bootstrappedMappings, boootstrappedOnto);
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

    private static List<String> getWorkloadQueries(String workloadFile) throws IOException {
        String json = Files.lines(Paths.get(workloadFile)).collect(Collectors.joining(" "));

        List<String> result = new ArrayList<>();

        JsonElement jsonElement = JsonParser.parseString(json);

        Gson g = new Gson();

        if (jsonElement.isJsonArray()) {

            JsonArray jsonArray = jsonElement.getAsJsonArray();

            for (JsonElement element : jsonArray) {
                String jsonStringElement = element.toString();
                WorkloadJsonEntry workloadJsonEntry = g.fromJson(jsonStringElement, WorkloadJsonEntry.class);
                result.add(workloadJsonEntry.getQuery());
            }
        }
        return result;
    }

}
