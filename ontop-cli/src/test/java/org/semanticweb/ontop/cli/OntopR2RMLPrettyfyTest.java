package org.semanticweb.ontop.cli;

import org.junit.Test;

public class OntopR2RMLPrettyfyTest {


    //@Ignore("too expensive to run")
    @Test
    public void testOntopHelp (){
        String[] argv = {"help", "mapping", "pretty-r2rml"};
        Ontop.main(argv);
    }

    //@Ignore("too expensive to run")
    @Test
    public void testOntopR2RMLToOBDA (){
        String[] argv = {"mapping", "pretty-r2rml",
                "-i", "src/test/resources/ugly-mapping-northwind.ttl",
                "-o", "src/test/resources/pretty-mapping-northwind.ttl"
        };
        Ontop.main(argv);
    }



}
