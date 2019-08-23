package it.unibz.inf.ontop.cli;

import org.junit.Test;

public class OntopR2RMLPrettyfyTest {


    @Test
    public void testOntopHelp (){
        String[] argv = {"help", "mapping", "pretty-r2rml"};
        Ontop.main(argv);
    }


    @Test
    public void testOntopR2RMLToOBDA (){
        String[] argv = {"mapping", "pretty-r2rml",
                "-i", "src/test/resources/ugly-mapping-northwind.ttl",
                "-o", "src/test/resources/output/pretty-mapping-northwind.ttl"
        };
        Ontop.main(argv);
    }



}
