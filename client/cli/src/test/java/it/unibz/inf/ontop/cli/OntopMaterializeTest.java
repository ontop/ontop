package it.unibz.inf.ontop.cli;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class OntopMaterializeTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();

    @Test
    public void testOntopHelpMaterialize(){
        Ontop.main("help", "materialize");
    }

//    @Ignore("too expensive")
//    @Test
//    public void testOntopMaterializeSeparateFiles (){
//        String[] argv = {"materialize",
//                "-m", "/Users/xiao/Projects/npd-benchmark/mappings/postgres/no-spatial/npd-v2-ql_a_postgres.obda",
//                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",
//                "-f", "turtle", "-o", "/tmp/npd", "--separate-files"};
//        Ontop.main(argv);
//    }
//
//    @Ignore("too expensive")
//    @Test
//    public void testOntopMaterializeSingleFile (){
//        String[] argv = {"materialize",
//                "-m", "/Users/xiao/Projects/npd-benchmark/mappings/postgres/no-spatial/npd-v2-ql_a_postgres.obda",
//                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",
//                "-f", "turtle", "-o", "/tmp/npd/npd.ttl"};
//        Ontop.main(argv);
//    }
//
//    @Ignore("too expensive")
//    @Test
//    public void testOntopMaterializeR2RML (){
//        String[] argv = {"materialize",
//                "-m", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.ttl",
//                "-t", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.owl",
//                "-f", "turtle", "-o", "/tmp/univ-benchQL-triples.ttl",
//                "-l",	"jdbc:mysql://10.7.20.39/lubm1",
//                "-u",	"fish",
//                "-p",	"fish",
//                "-d",	"com.mysql.jdbc.Driver"
//        };
//        Ontop.main(argv);
//    }
//
//    @Ignore("too expensive")
//    @Test
//    public void testOntopMaterializeR2RMLNoOntology (){
//        String[] argv = {"materialize",
//                "-m", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.ttl",
//                "-f", "turtle", "-o", "/tmp/univ-benchQL-triples.ttl",
//                "-l",	"jdbc:mysql://10.7.20.39/lubm1",
//                "-u",	"fish",
//                "-p",	"fish",
//                "-d",	"com.mysql.jdbc.Driver"
//        };
//        Ontop.main(argv);
//    }

    @Test
    public void testOntopMaterializeNoStreamResults (){
        Ontop.main("materialize", "-m", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-f", "turtle",
                "-o", "src/test/resources/output/exampleBooks.materialized.nostreaming.ttl",
                "--no-streaming");
    }

    @Test
    public void testOntopMaterialize (){
        Ontop.main("materialize", "-m", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-f", "turtle",
                "-o", "src/test/resources/output/exampleBooks.materialized.ttl");
    }

    @Test
    public void testOntopMaterializeNTriples (){
        Ontop.main("materialize", "-m", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-f", "ntriples",
                "-o", "src/test/resources/output/exampleBooks.materialized.nt");
    }

    @Test
    public void testOntopMaterializeJsonLD (){
        Ontop.main("materialize", "-m", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-f", "jsonld",
                "-o", "src/test/resources/output/exampleBooks.materialized.jsonld");
    }
    
    @Test
    public void testOntopMaterializeSeparatefiles (){
        Ontop.main("materialize", "-m", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-f", "turtle",
                "-o", "src/test/resources/output/",
                "--separate-files");
    }

    @Test
    public void testOntopMaterializeSeparatefilesNoFormat (){
        Ontop.main("materialize", "-m", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-o", "src/test/resources/output/",
                "--separate-files");
    }
}
