package org.semanticweb.ontop.cli;

import org.junit.Test;

public class OntopBootstrapTest {


    @Test
    public void testOntopBootstrap (){
        String[] argv = {"bootstrap",
                "-b", "http://www.example.org/",
                "-m", "/tmp/bootstrapped-univ-benchQL.obda",
                "-t", "/tmp/bootstrapped-univ-benchQL.owl",
                "-l", "jdbc:mysql://10.7.20.39/lubm1",
                "-u", "fish",
                "-p", "fish",
                "-d", "com.mysql.jdbc.Driver"
        };
        Ontop.main(argv);
    }


}
