package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.Cli;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.cli.Ontop.getOntopCommandCLI;

public class OntopOBDAToR2RMLTest {

    @Test
    public void testOntopHelp (){
        Ontop.main("help", "mapping", "to-r2rml");
    }

    @Ignore("avoids overwriting exampleBooks.ttl")
    @Test
    public void testOntopOBDAToR2RML (){
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl");
    }

    @Test
    public void testOntopOBDAToR2RML1 (){
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/templateExample.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-o", "src/test/resources/output/templateExample.r2rml");
    }

    @Test
    public void testOntopOBDAToR2RML2 (){
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-northwind.obda",
                "-t", "src/test/resources/mapping-northwind.owl",
                "-o", "src/test/resources/output/mapping-northwind.r2rml");
    }

    @Test
    public void testOntopOBDAToR2RML_NoOntology (){
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/books/exampleBooks.obda",
                "-o", "src/test/resources/output/exampleBooks.r2rml");
    }

    @Test
    public void testOntopR2RML2OBDA2R2RML (){
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-northwind-named-graph.obda",
                "-o", "src/test/resources/output/mapping-northwind-named-graph.r2rml");
    }

    @Test
    public void testOntopOBDA2R2RML_quotation_marks (){
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-booktutorial.obda",
                "-o", "src/test/resources/output/mapping-booktutorial.ttl");
    }

    @Test
    public void testOntopOBDA2R2RML_prof_bnode(){
        Ontop.main("mapping", "to-r2rml",
                "-i", "src/test/resources/prof/prof-bnode.obda",
                "-o", "target/prof-bnode-mapping.ttl");
    }
}
