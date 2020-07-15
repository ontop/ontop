package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.Cli;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.cli.Ontop.getOntopCommandCLI;

public class OntopOBDAToR2RMLTest {

    @Test
    public void testOntopHelp (){
        String[] argv = {"help", "mapping", "to-r2rml"};
        runCommand(argv);
    }

    @Ignore("avoids overwriting exampleBooks.ttl")
    @Test
    public void testOntopOBDAToR2RML (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl"
        };
        runCommand(argv);
    }



    @Test
    public void testOntopOBDAToR2RML1 (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/templateExample.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-o", "src/test/resources/output/templateExample.r2rml"
        };
        runCommand(argv);
    }

    @Test
    public void testOntopOBDAToR2RML2 (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-northwind.obda",
                "-t", "src/test/resources/mapping-northwind.owl",
                "-o", "src/test/resources/output/mapping-northwind.r2rml"
        };
        runCommand(argv);
    }

    @Test
    public void testOntopOBDAToR2RML_NoOntology (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/books/exampleBooks.obda",
                "-o", "src/test/resources/output/exampleBooks.r2rml"
        };
        runCommand(argv);
    }

    @Test
    public void testOntopR2RML2OBDA2R2RML (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-northwind-named-graph.obda",
                "-o", "src/test/resources/output/mapping-northwind-named-graph.r2rml"
        };
        runCommand(argv);
    }

    @Test
    public void testOntopOBDA2R2RML_quotation_marks (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-booktutorial.obda",
                "-o", "src/test/resources/output/mapping-booktutorial.ttl"
        };
        runCommand(argv);
    }


    private void runCommand(String[] args) {
        Cli<OntopCommand> ontopCommandCLI = getOntopCommandCLI();

        OntopCommand command = ontopCommandCLI.parse(args);
        command.run();
    }

}
