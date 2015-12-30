package org.semanticweb.ontop.cli;


import org.junit.Ignore;
import org.junit.Test;


public class OntopTest {

    @Test
    public void testOntopMainNoArgs(){
        Ontop.main();
    }

    @Test
    public void testOntopHelp(){
        Ontop.main("help");
    }


    @Test
    public void testOntopHelpMapping(){
        Ontop.main("help", "mapping");
    }

    @Test
    public void testOntopHelpMapping_ToR2rml(){
        Ontop.main("help", "mapping", "to-r2rml");
    }

    @Test
    public void testOntopHelpMapping_ToOBDA(){
        Ontop.main("help", "mapping", "to-obda");
    }

    @Test
    public void testOntopHelpMapping_Prettify(){
        Ontop.main("help", "mapping", "pretty-r2rml");
    }

    @Test
    public void testOntopMissingCommand (){
        String[] argv = { "-m", "/Users/xiao/Projects/npd-benchmark/mappings/postgres/no-spatial/npd-v2-ql_a_postgres.obda",
                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",
                "-f", "turtle", "-o", "/tmp/npd", "--separate-files"};
        Ontop.main(argv);
    }

    @Test
    public void testOntopMissingArgValues (){
        String[] argv = { "materialize", "-m",
                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",
                "-f", "turtle", "-o", "/tmp/npd", "--separate-files"};
        Ontop.main(argv);
    }

    @Test
    public void testOntopMissingRequiredArg (){
        String[] argv = { "materialize",
                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",
                "-f", "turtle", "-o", "/tmp/npd", "--separate-files"};
        Ontop.main(argv);
    }


}
