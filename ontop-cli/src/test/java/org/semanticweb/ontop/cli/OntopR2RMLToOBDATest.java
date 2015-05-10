package org.semanticweb.ontop.cli;

import org.junit.Test;

public class OntopR2RMLToOBDATest {


    //@Ignore("too expensive to run")
    @Test
    public void testOntopHelp (){
        String[] argv = {"help", "mapping", "to-obda"};
        Ontop.main(argv);
    }

    //@Ignore("too expensive to run")
    @Test
    public void testOntopR2RMLToOBDA (){
        String[] argv = {"mapping", "to-obda",
                "-i", "src/test/resources/mapping-northwind.ttl",
                "-o", "src/test/resources/out-mapping-northwind.obda"
        };
        Ontop.main(argv);
    }



}
