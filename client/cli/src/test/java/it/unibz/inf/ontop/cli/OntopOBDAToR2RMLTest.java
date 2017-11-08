package it.unibz.inf.ontop.cli;

import org.junit.Ignore;
import org.junit.Test;

public class OntopOBDAToR2RMLTest {

    @Test
    public void testOntopHelp (){
        String[] argv = {"help", "mapping", "to-r2rml"};
        Ontop.main(argv);
    }

    @Ignore("avoids overwriting exampleBooks.ttl")
    @Test
    public void testOntopOBDAToR2RML (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl"
        };
        Ontop.main(argv);
    }



    @Test
    public void testOntopOBDAToR2RML1 (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/templateExample.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-o", "src/test/resources/output/templateExample.r2rml"
        };
        Ontop.main(argv);
    }

    @Test
    public void testOntopOBDAToR2RML2 (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-northwind.obda",
                "-t", "src/test/resources/mapping-northwind.owl",
                "-o", "src/test/resources/output/mapping-northwind.r2rml"
        };
        Ontop.main(argv);
    }

    @Test
    public void testOntopOBDAToR2RML_NoOntology (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/books/exampleBooks.obda",
                "-o", "src/test/resources/output/exampleBooks.r2rml"
        };
        Ontop.main(argv);
    }

    @Test
    public void testOntopOBDAToR2RML3 (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/oreda_bootstrapped_mapping.obda",
                "-t", "src/test/resources/oreda_bootstrapped_ontology.owl",
                "-o", "src/test/resources/output/oreda_bootstrapped_mapping.r2rml"
        };
        Ontop.main(argv);
    }


}
