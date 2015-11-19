package org.semanticweb.ontop.cli;

import org.junit.Test;

public class OntopBootstrapTest {

    @Test
    public void testOntopHelp(){
        Ontop.main("help", "bootstrap");
    }


    @Test
    public void testOntopBootstrap (){
        String[] argv = {"bootstrap",
                "-b", "http://www.example.org/",
                "-m", "src/test/resources/bootstrapped-univ-benchQL.obda",
                "-t", "src/test/resources/bootstrapped-univ-benchQL.owl",
                "-l", "jdbc:mysql://10.7.20.39/lubm1",
                "-u", "fish",
                "-p", "fish",
                "-d", "com.mysql.jdbc.Driver"
        };
        Ontop.main(argv);
    }

    @Test
    public void testOntopBootstrapR2RML (){
        String[] argv = {"bootstrap",
                "-b", "http://www.example.org/",
                "-m", "src/test/resources/npd.ttl",
                "-t", "src/test/resources/npd.owl",
                "-l", "jdbc:postgresql://10.7.20.39/npd_no_spatial",
                "-u", "postgres",
                "-p", "postgres",
                "-d", "org.postgresql.Driver"
        };
        Ontop.main(argv);
    }
}
