package it.unibz.inf.ontop.cli;

import org.junit.Test;

public class OntopOBDACleanupTest {

    @Test
    public void testOntopHelp (){
        String[] argv = {"help", "mapping", "obda-cleanup"};
        Ontop.main(argv);
    }

    @Test
    public void OntopOBDACleanup (){
        String[] argv = {"mapping", "obda-cleanup",
                "-m", "src/test/resources/bootstrapped-univ-benchQL.obda",
                "-o", "src/test/resources/output/bootstrapped-univ-benchQL.obda"
        };
        Ontop.main(argv);
    }

    @Test
    public void OntopOBDACleanupNoOutput (){
        String[] argv = {"mapping", "obda-cleanup",
                "-m", "src/test/resources/bootstrapped-univ-benchQL.obda"
        };
        Ontop.main(argv);
    }

    @Test
    public void OntopOBDACleanupProjection (){
        String[] argv = {"mapping", "obda-cleanup",
                "-m", "src/test/resources/books/exampleBooks.obda",
                "-o", "src/test/resources/output/exampleBooks-cleanup.obda"
                ,"--simplify-projection"
        };
        Ontop.main(argv);
    }

}
