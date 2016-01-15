package org.semanticweb.ontop.cli;

import org.junit.Test;


public class OntopValidateTest {
    @Test
    public void testOntopValidateCMD (){
        String[] argv = {"validate", "-m", "/Users/xiao/Downloads/tmp/rais-dc-new.obda",
                "-t", "/Users/xiao/Downloads/tmp/rais-dc-new.owl"};
        Ontop.main(argv);
    }
}