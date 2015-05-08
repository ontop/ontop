package org.semanticweb.ontop.cli;

import org.junit.Test;

public class OntopOBDAToR2RMLTest {


    //@Ignore("too expensive to run")
    @Test
    public void testOntopHelp (){
        String[] argv = {"help", "mapping", "to-r2rml"};
        Ontop.main(argv);
    }

    //@Ignore("too expensive to run")
    @Test
    public void testOntopOBDAToR2RML (){
        String[] argv = {"mapping", "to-r2rml",
                "-i", "src/test/resources/mapping-northwind.obda",
                "-t", "src/test/resources/mapping-northwind.owl"
        };
        Ontop.main(argv);
    }




}
