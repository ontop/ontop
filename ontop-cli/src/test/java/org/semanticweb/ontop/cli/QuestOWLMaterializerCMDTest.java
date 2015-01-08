package org.semanticweb.ontop.cli;

import org.junit.Ignore;
import org.junit.Test;

public class QuestOWLMaterializerCMDTest {

    @Ignore("too expensive to run")
    @Test
    public void testMaterializerCMD_noOntology (){
        //String[] argv = {"-obda", "../obdalib-r2rml/src/test/resources/npd-v2-ql_a.obda", "-format", "turtle", "-output", "npd-v2.ttl"};
        String[] argv = {"-obda", "/Users/xiao/Projects/npd-benchmark/mappings/postgres/no-spatial/npd-v2-ql_a_postgres.obda", "-format", "turtle", "-output", "/tmp/npd-v2_no_spatial.ttl"};
        QuestOWLMaterializerCMD.main(argv);
    }

    //@Test



}
