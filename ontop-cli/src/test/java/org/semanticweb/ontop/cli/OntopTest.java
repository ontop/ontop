package org.semanticweb.ontop.cli;

import org.junit.Test;


public class OntopTest {

    @Test
    public void testOntopMain(){
        Ontop.main(new String[]{});
    }

    @Test
    public void testOntopHelpMaterialize(){
        Ontop.main(new String[]{"help", "materialize"});
    }
}
