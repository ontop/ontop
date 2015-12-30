package org.semanticweb.ontop.cli;

import org.junit.Ignore;
import org.junit.Test;

public class OntopMaterializeTest {

    @Test
    public void testOntopHelpMaterialize(){
        Ontop.main("help", "materialize");
    }


    @Ignore("too expensive")
    @Test
    public void testOntopMaterializeSeparateFiles (){
        String[] argv = {"materialize",
                "-m", "/Users/xiao/Projects/npd-benchmark/mappings/postgres/no-spatial/npd-v2-ql_a_postgres.obda",
                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",
                "-f", "turtle", "-o", "/tmp/npd", "--separate-files"};
        Ontop.main(argv);
    }

    @Ignore("too expensive")
    @Test
    public void testOntopMaterializeSingleFile (){
        String[] argv = {"materialize",
                "-m", "/Users/xiao/Projects/npd-benchmark/mappings/postgres/no-spatial/npd-v2-ql_a_postgres.obda",
                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",
                "-f", "turtle", "-o", "/tmp/npd/npd.ttl"};
        Ontop.main(argv);
    }

    @Ignore("too expensive")
    @Test
    public void testOntopMaterializeR2RML (){
        String[] argv = {"materialize",
                "-m", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.ttl",
                "-t", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.owl",
                "-f", "turtle", "-o", "/tmp/univ-benchQL-triples.ttl",
                "-l",	"jdbc:mysql://10.7.20.39/lubm1",
                "-u",	"fish",
                "-p",	"fish",
                "-d",	"com.mysql.jdbc.Driver"
        };
        Ontop.main(argv);
    }

    @Ignore("too expensive")
    @Test
    public void testOntopMaterializeR2RMLNoOntology (){
        String[] argv = {"materialize",
                "-m", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.ttl",
                "-f", "turtle", "-o", "/tmp/univ-benchQL-triples.ttl",
                "-l",	"jdbc:mysql://10.7.20.39/lubm1",
                "-u",	"fish",
                "-p",	"fish",
                "-d",	"com.mysql.jdbc.Driver"
        };
        Ontop.main(argv);
    }

    @Ignore("too expensive")
    @Test
    public void testOntopMaterializeR2RMLSeparateFiles (){
        String[] argv = {"materialize", "-m", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.ttl",
                "-t", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.owl",
                "-f", "turtle", "-o", "/tmp/",
                "-U",	"jdbc:mysql://10.7.20.39/lubm1",
                "-u",	"fish",
                "-p",	"fish",
                "-d",	"com.mysql.jdbc.Driver",
                "--separate-files"
        };
        Ontop.main(argv);
    }

}
