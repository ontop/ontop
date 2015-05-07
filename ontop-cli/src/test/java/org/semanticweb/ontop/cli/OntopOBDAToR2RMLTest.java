package org.semanticweb.ontop.cli;

import org.junit.Test;

public class OntopOBDAToR2RMLTest {


    //@Ignore("too expensive to run")
    @Test
    public void testOntopQueryCMD (){
        String[] argv = {"query", "-m", "/Users/xiao/Projects/npd-benchmark/mappings/postgres/no-spatial/npd-v2-ql_a_postgres.obda",
                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",  "-q", "/Users/xiao/Projects/npd-benchmark/queries/01.q"};
        Ontop.main(argv);
    }

    //@Ignore("too expensive to run")
    @Test
    public void testOntopQueryCMD_Out (){
        String[] argv = {"query",
                "-m", "/Users/xiao/Projects/npd-benchmark/mappings/postgres/no-spatial/npd-v2-ql_a_postgres.obda",
                "-t", "/Users/xiao/Projects/npd-benchmark/ontology/vocabulary.owl",
                "-q", "/Users/xiao/Projects/npd-benchmark/queries/01.q",
                "-o", "/tmp/q1.csv"};
        Ontop.main(argv);
    }

    //@Ignore("too expensive")
    @Test
    public void testOntopQueryR2RML (){
        String[] argv = {"query",
                "-m", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.ttl",
                "-t", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.owl",
                "-o", "/tmp/q1.csv",
                "-l", "jdbc:mysql://10.7.20.39/lubm1",
                "-u", "fish",
                "-p", "fish",
                "-d", "com.mysql.jdbc.Driver",
                "-q", "/Users/xiao/Projects/iswc2014-benchmark/Ontop/q1.txt"
        };
        Ontop.main(argv);
    }

    @Test
    public void testOntopQueryR2RML_noOntology (){
        String[] argv = {"query",
                "-m", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.ttl",
                //"-t", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.owl",
                "-o", "/tmp/q5_noOntology.csv",
                "-l",	"jdbc:mysql://10.7.20.39/lubm1",
                "-u",	"fish",
                "-p",	"fish",
                "-d",	"com.mysql.jdbc.Driver",
                "-q", "/Users/xiao/Projects/iswc2014-benchmark/Ontop/q5.txt"
        };
        Ontop.main(argv);
    }

    @Test
    public void testOntopQueryR2RML_Ontology (){
        String[] argv = {"query",
                "-m", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.ttl",
                "-t", "/Users/xiao/Projects/iswc2014-benchmark/LUBM/univ-benchQL.owl",
                "-o", "/tmp/q5_Ontology.csv",
                "-l",	"jdbc:mysql://10.7.20.39/lubm1",
                "-u",	"fish",
                "-p",	"fish",
                "-d",	"com.mysql.jdbc.Driver",
                "-q", "/Users/xiao/Projects/iswc2014-benchmark/Ontop/q5.txt"
        };
        Ontop.main(argv);
    }



}
