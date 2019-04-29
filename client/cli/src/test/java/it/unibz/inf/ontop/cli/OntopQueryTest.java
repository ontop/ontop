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
        String[] argv = {"query", "-m", "src/test/resources/books/exampleBooks.obda",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-q", "src/test/resources/books/q1.rq"};
        Ontop.main(argv);
    }


    @Test
    public void testOntopQueryCMD_Out (){
        String[] argv = {"query", "-m", "src/test/resources/books/exampleBooks.obda",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-q", "src/test/resources/books/q1.rq",
                "-o", "src/test/resources/output/q1-answer.csv"};
        Ontop.main(argv);
    }

    @Test
    public void testOntopQueryR2RML (){
        String[] argv = {"query", "-m", "src/test/resources/books/exampleBooks.ttl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-q", "src/test/resources/books/q1.rq"};
        Ontop.main(argv);
    }

    @Test
    public void testOntopQueryR2RML_noOntology (){
        String[] argv = {"query",
                "-m", "src/test/resources/books/exampleBooks.ttl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-q", "src/test/resources/books/q1.rq"};
        Ontop.main(argv);
    }

    @Ignore("too expensive")
    @Test
    public void testOntopQueryAnnotations_Ontology (){
        String[] argv = {"query",
                "-m", "src/test/resources/pgsql/annotation/doid.obda",
                "-t", "src/test/resources/pgsql/annotation/doid.owl",
                "-q", "src/test/resources/pgsql/annotation/q1.q",
                "--enable-annotations"
        };
        Ontop.main(argv);
     }


    @Test
    public void temp(){
        String[] argv = {"query",
                "-m", "/home/julien/Desktop/ontop_ex/university-complete.obda",
                "-p", "/home/julien/Desktop/ontop_ex/university-complete.properties",
                "-q", "/home/julien/Desktop/ontop_ex/university-complete.q",
                "-t", "/home/julien/Desktop/ontop_ex/university-complete.ttl"
        };
        Ontop.main(argv);
    }

}
