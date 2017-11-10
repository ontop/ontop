package it.unibz.inf.ontop.cli;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class OntopBootstrapTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();

    @Test
    public void testOntopHelp(){
        Ontop.main("help", "bootstrap");
    }


    @Test
    public void testOntopBootstrap (){
        String[] argv = {"bootstrap",
                "-b", "http://www.example.org/",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-m", "src/test/resources/output/exampleBooks-bootstrapped.obda",
                "-t", "src/test/resources/output/exampleBooks-bootstrapped.owl"
        };
        Ontop.main(argv);
    }


}
