package it.unibz.inf.ontop.cli;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class OntopExtractDBMetadataTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();

    @Test
    public void testOntopHelp(){
        Ontop.main("help", "extract-db-metadata");
    }


    @Test
    public void testOntopExtractDBMetadata(){
        Ontop.main("extract-db-metadata",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-o", "src/test/resources/output/exampleBooks-metadata.json");
    }


}
