package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.Cli;
import org.junit.Test;

import static it.unibz.inf.ontop.cli.Ontop.getOntopCommandCLI;

public class OntopR2RMLToOBDATest {


    //@Ignore("too expensive to run")
    @Test
    public void testOntopHelp (){
        String[] argv = {"help", "mapping", "to-obda"};
        runCommand(argv);
    }

    //@Ignore("too expensive to run")
    @Test
    public void testOntopR2RMLToOBDA (){
        String[] argv = {"mapping", "to-obda",
                "-i", "src/test/resources/books/exampleBooks.ttl",
                "-o", "src/test/resources/output/converted-exampleBooks.obda"
        };
        runCommand(argv);
    }

    @Test
    public void testOntopR2RMLToOBDA2 (){
        String[] argv = {"mapping", "to-obda",
                "-i", "src/test/resources/mapping.ttl",
                "-o", "src/test/resources/output/mapping-booktutorial.obda"
        };
        runCommand(argv);
    }

    @Test
    public void testOntopR2RMLToOBDA3 (){
        String[] argv = {"mapping", "to-obda",
                "-i", "src/test/resources/mapping-blankNode.ttl",
                "-o", "src/test/resources/output/mapping-blankNode.obda"
        };
        runCommand(argv);
    }

    @Test
    public void testOntopR2RMLToOBDANamedGraph (){
        String[] argv = {"mapping", "to-obda",
                "-i", "src/test/resources/mapping-named-graph.ttl",
                "-o", "src/test/resources/output/mapping-named-graph.obda"
        };
        runCommand(argv);
    }

    @Test
    public void testOntopR2RML2OBDA_prof_bnode(){
        String[] argv = {"mapping", "to-obda",
                "-i", "src/test/resources/prof/prof-bnode.mapping.ttl",
                "-o", "target/prof-bnode.obda"
        };
        runCommand(argv);
    }

    private void runCommand(String[] args) {
        Cli<OntopCommand> ontopCommandCLI = getOntopCommandCLI();

        OntopCommand command = ontopCommandCLI.parse(args);
        command.run();
    }



}
