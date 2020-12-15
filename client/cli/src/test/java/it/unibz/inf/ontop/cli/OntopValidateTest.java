package it.unibz.inf.ontop.cli;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExternalResource;

@Ignore
public class OntopValidateTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();

    @Test
    public void testOntopValidateCMD (){
        Ontop.main("validate", "-m", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-p",  "src/test/resources/books/exampleBooks.properties");
    }
}