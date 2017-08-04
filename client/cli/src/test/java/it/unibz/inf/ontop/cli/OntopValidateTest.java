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
        String[] argv = {"validate", "-m", "client/cli/src/test/resources/books/exampleBooks.obda",
                "-t", "client/cli/src/test/resources/books/exampleBooks.owl",
        "-p",  "client/cli/src/test/resources/books/exampleBooks.properties" };
        Ontop.main(argv);
    }
}