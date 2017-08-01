package it.unibz.inf.ontop.cli;

import org.junit.Test;

public class OntopR2RMLCleanupTest {

    @Test
    public void testOntopHelp (){
        String[] argv = {"help", "mapping", "r2rml-cleanup"};
        Ontop.main(argv);
    }

    @Test
    public void OntopR2RMLCleanup (){
        String[] argv = {"mapping", "r2rml-cleanup",
                "-m", "src/test/resources/npd-v2-ql-mysql-ontop1.17.ttl"
        };
        Ontop.main(argv);
    }


    @Test
    public void OntopR2rmlCleanupAllSetting (){
        String[] argv = {"mapping", "r2rml-cleanup",
                "-m", "src/test/resources/npd-v2-ql-mysql-ontop1.17.ttl",
                "-o", "src/test/resources/output/npd-v2-ql-mysql-ontop1.17.ttl",
                "--prettify", "--templates-to-columns", "--replace-to-simple"
        };
        Ontop.main(argv);
    }


    @Test
    public void OntopR2rmlCleanupProjection (){
        String[] argv = {"mapping", "r2rml-cleanup",
                "-m", "src/test/resources/books/exampleBooks.ttl",
                "-o", "src/test/resources/output/exampleBooks.ttl",
                "--simplify-projection"
        };
        Ontop.main(argv);
    }


}
