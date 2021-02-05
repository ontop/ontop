package it.unibz.inf.ontop.cli;


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
    public void testOntopHelpEndpoint(){
        Ontop.main("help", "endpoint");
    }

    @Test
    public void testOntopHelpMapping(){
        Ontop.main("help", "mapping");
    }

    @Test
    public void testOntopHelpMaterialize(){
        Ontop.main("help", "materialize");
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
        Ontop.main("-m", "/Users/xiao/Projects/npd-benchmark/mappings/postgres/no-spatial/npd-v2-ql_a_postgres.obda",
                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",
                "-f", "turtle", "-o", "/tmp/npd", "--separate-files");
    }

    @Test
    public void testOntopMissingArgValues (){
        Ontop.main("materialize", "-m",
                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",
                "-f", "turtle",
                "-o", "/tmp/npd",
                "--separate-files");
    }

    @Test
    public void testOntopMissingRequiredArg (){
        Ontop.main("materialize",
                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",
                "-f", "turtle",
                "-o", "/tmp/npd",
                "--separate-files");
    }
}
