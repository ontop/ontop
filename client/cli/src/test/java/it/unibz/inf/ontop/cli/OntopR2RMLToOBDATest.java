package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.Cli;
import org.junit.Test;

import static it.unibz.inf.ontop.cli.Ontop.getOntopCommandCLI;

public class OntopR2RMLToOBDATest {


    @Test
    public void testOntopHelp (){
        Ontop.main("help", "mapping", "to-obda");
    }

    //@Ignore("too expensive to run")
    @Test
    public void testOntopR2RMLToOBDA (){
        Ontop.main("mapping", "to-obda",
                "-i", "src/test/resources/books/exampleBooks.ttl",
                "-o", "src/test/resources/output/converted-exampleBooks.obda");
    }

    @Test
    public void testOntopR2RMLToOBDA2 (){
        Ontop.main("mapping", "to-obda",
                "-i", "src/test/resources/mapping.ttl",
                "-o", "src/test/resources/output/mapping-booktutorial.obda");
    }

    @Test
    public void testOntopR2RMLToOBDA3 (){
        Ontop.main("mapping", "to-obda",
                "-i", "src/test/resources/mapping-blankNode.ttl",
                "-o", "src/test/resources/output/mapping-blankNode.obda");
    }

    @Test
    public void testOntopR2RMLToOBDANamedGraph (){
        Ontop.main("mapping", "to-obda",
                "-i", "src/test/resources/mapping-named-graph.ttl",
                "-o", "src/test/resources/output/mapping-named-graph.obda");
    }

    @Test
    public void testOntopR2RML2OBDA_prof_bnode(){
        Ontop.main("mapping", "to-obda",
                "-i", "src/test/resources/prof/prof-bnode.mapping.ttl",
                "-o", "target/prof-bnode.obda");
    }
}
