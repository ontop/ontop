package it.unibz.inf.ontop.cli;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExternalResource;

@Ignore
public class OntopQueryTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();

    @Test
    public void testOntopQueryCMD (){
        Ontop.main("query", "-m", "src/test/resources/books/exampleBooks.obda",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-q", "src/test/resources/books/q1.rq");
    }


    @Test
    public void testOntopQueryCMD_Out (){
        Ontop.main("query", "-m", "src/test/resources/books/exampleBooks.obda",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-q", "src/test/resources/books/q1.rq",
                "-o", "src/test/resources/output/q1-answer.csv");
    }

    @Test
    public void testOntopQueryR2RML (){
        Ontop.main("query", "-m", "src/test/resources/books/exampleBooks.ttl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-q", "src/test/resources/books/q1.rq");
    }

    @Test
    public void testOntopQueryR2RML_noOntology (){
        Ontop.main("query",
                "-m", "src/test/resources/books/exampleBooks.ttl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-q", "src/test/resources/books/q1.rq");
    }

    @Ignore("too expensive")
    @Test
    public void testOntopQueryAnnotations_Ontology (){
        Ontop.main("query",
                "-m", "src/test/resources/pgsql/annotation/doid.obda",
                "-t", "src/test/resources/pgsql/annotation/doid.owl",
                "-q", "src/test/resources/pgsql/annotation/q1.q",
                "--enable-annotations");
     }

}
