package it.unibz.inf.ontop.cli;

import org.junit.Test;

public class OntopR2RMLPrettyfyTest {

    @Test
    public void testOntopHelp (){
        Ontop.main("help", "mapping", "pretty-r2rml");
    }

    @Test
    public void testOntopR2RMLToOBDA (){
        Ontop.main("mapping", "pretty-r2rml",
                "-i", "src/test/resources/ugly-mapping-northwind.ttl",
                "-o", "src/test/resources/output/pretty-mapping-northwind.ttl");
    }
}
