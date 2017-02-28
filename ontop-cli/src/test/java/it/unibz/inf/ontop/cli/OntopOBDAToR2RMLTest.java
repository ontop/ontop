package it.unibz.inf.ontop.cli;

import org.junit.Test;

public class OntopOBDAToR2RMLTest {

    @Test
    public void testOntopHelp (){
        String[] argv = {"help", "mapping", "to-r2rml"};
        Ontop.main(argv);
    }

    @Test
    public void testOntopOBDAToR2RML (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl"
        };
        Ontop.main(argv);
    }

    @Test
    public void testOntopOBDAToR2RML2 (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-northwind.obda",
                "-t", "src/test/resources/mapping-northwind.owl",
                "-o", "src/test/resources/mapping-northwind.r2rml"
        };
        Ontop.main(argv);
    }

    @Test
    public void testOntopOBDAToR2RML_NoOntology (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/books/exampleBooks.obda",
                "-o", "src/test/resources/books/exampleBooks.r2rml"
        };
        Ontop.main(argv);
    }



}
