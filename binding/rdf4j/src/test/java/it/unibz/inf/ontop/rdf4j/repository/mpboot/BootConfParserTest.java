package it.unibz.inf.ontop.rdf4j.repository.mpboot;

import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.bootconfparser.BootConfParser;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.clusters.Cluster;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.dictionary.Dictionary;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.shdefinitions.SHDefinition;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Lanti Davide
 */
public class BootConfParserTest {

    // Labels test
    private final static String CONF_FILE = "src/test/resources/bootstrapper.bootConfTest/boot-conf.json";
    private final static String REFERENCE_DICT_FILE = "src/test/resources/bootstrapper.bootConfTest/expected-dict.txt";
    private final static String ACTUAL_DICT_FILE = "src/test/resources/bootstrapper.bootConfTest/actual-dict.txt";

    private final static String REF_SH = "[SHDefinition{parent='parent1', child='child1'}, SHDefinition{parent='parent2', child='child2'}]";

    private final static String REF_CLUSTERS_FILE = "src/test/resources/bootstrapper.bootConfTest/expected-clusters.txt";
    private final static String ACTUAL_CLUSTERS_FILE = "src/test/resources/bootstrapper.bootConfTest/actual-clusters.txt";

    @Test
    public void parseTest(){
        try {
            Dictionary dict = BootConfParser.parseDictionary(CONF_FILE);
            List<SHDefinition> shDefs = BootConfParser.parseSHDefs(CONF_FILE);
            boolean shActive = BootConfParser.parseEnableSH(CONF_FILE);
            BootConf.NullValue nullValue = BootConfParser.parseNullValue(CONF_FILE);
            List<BootConf.GenerateOnlyEntry> generateOnlyEntries = BootConfParser.parseGenerateOnly(CONF_FILE);
            List<Cluster> clusters = BootConfParser.parseClustering(CONF_FILE);

            assertEquals(shDefs.toString(), REF_SH);

            assertEquals(generateOnlyEntries.size(), 2);
            assertEquals(generateOnlyEntries.get(0).toString(), "GenerateOnlyEntry{tableName='name', schemaName='schemaName', attributes=[att1, att2, att3]}");
            assertEquals(generateOnlyEntries.get(1).toString(), "GenerateOnlyEntry{tableName='name1', schemaName='schemaName', attributes=[att1, att2, att3]}");

            File referenceDict = new File(REFERENCE_DICT_FILE);
            File actualDict = new File(ACTUAL_DICT_FILE);
            File referenceClusters = new File(REF_CLUSTERS_FILE);
            File actualClusters = new File(ACTUAL_CLUSTERS_FILE);

            FileUtils.openOutputStream(actualDict).write(dict.toString().getBytes());
            assertTrue(FileUtils.contentEquals(referenceDict, actualDict));

            FileUtils.openOutputStream(actualClusters).write(clusters.toString().getBytes());
            assertTrue(FileUtils.contentEquals(referenceClusters, actualClusters));

            assertEquals(shActive, true);
            assertEquals(nullValue.getStringNull(), "NULL");
            assertEquals(nullValue.getIntNull(), 0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
