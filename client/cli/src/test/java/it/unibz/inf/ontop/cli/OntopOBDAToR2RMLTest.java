package it.unibz.inf.ontop.cli;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExternalResource;


public class OntopOBDAToR2RMLTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();

    @Test
    public void testOntopHelp (){
        Ontop.main("help", "mapping", "to-r2rml");
    }

    @Ignore("avoids overwriting exampleBooks.ttl")
    @Test
    public void testOntopOBDAToR2RML() {
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "--force");
    }

    @Test
    public void testOntopOBDAToR2RML1() {
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/templateExample.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-o", "src/test/resources/output/templateExample.r2rml",
                "--force");
    }

    @Test
    public void testOntopOBDAToR2RML2() {
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-northwind.obda",
                "-t", "src/test/resources/mapping-northwind.owl",
                "-o", "src/test/resources/output/mapping-northwind.r2rml",
                "--force");
    }

    @Test
    public void testOntopOBDAToR2RML_NoOntology() {
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/books/exampleBooks.obda",
                "-o", "src/test/resources/output/exampleBooks.r2rml",
                "--force");
    }

    @Test
    public void testOntopR2RML2OBDA2R2RML() {
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-northwind-named-graph.obda",
                "-o", "src/test/resources/output/mapping-northwind-named-graph.r2rml",
                "--force");
    }

    @Test
    public void testOntopOBDA2R2RML_quotation_marks() {
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-booktutorial.obda",
                "-o", "src/test/resources/output/mapping-booktutorial.ttl",
                "--force");
    }

    @Test
    public void testOntopOBDA2R2RML_prof_bnode() {
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/prof/prof-bnode.obda",
                "-o", "src/test/resources/output/prof-bnode-mapping.ttl",
                "--force");
    }

    @Test
    public void testOntopR2RML2OBDA_withProperties() {
        Ontop.main("mapping", "to-r2rml",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-i", "src/test/resources/books/exampleBooks.obda",
                "-o", "src/test/resources/output/exampleBooks-properties.ttl");
    }
}
