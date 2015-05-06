package org.semanticweb.ontop.cli;

import org.junit.Ignore;
import org.junit.Test;

public class OntopQueryCMDTest {


    @Ignore("too expensive to run")
    @Test
    public void testOntopQueryCMD (){
        //String[] argv = {"-obda", "../obdalib-r2rml/src/test/resources/npd-v2-ql_a.obda", "-format", "turtle", "-output", "npd-v2.ttl"};
        String[] argv = {"-obda", "/Users/xiao/Projects/npd-benchmark/mappings/postgres/no-spatial/npd-v2-ql_a_postgres.obda",
                "-onto", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",  "-q", "/Users/xiao/Projects/npd-benchmark/queries/01.q"};
        OntopQueryCMD.main(argv);
    }

    @Ignore("too expensive to run")
    @Test
    public void testOntopQueryCMD_Out (){
        //String[] argv = {"-obda", "../obdalib-r2rml/src/test/resources/npd-v2-ql_a.obda", "-format", "turtle", "-output", "npd-v2.ttl"};
        String[] argv = {"-obda", "/Users/xiao/Projects/npd-benchmark/mappings/postgres/no-spatial/npd-v2-ql_a_postgres.obda",
                "-onto", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",  "-q", "/Users/xiao/Projects/npd-benchmark/queries/01.q", "-out", "/tmp/q1.csv"};
        OntopQueryCMD.main(argv);
    }



}
