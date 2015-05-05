package org.semanticweb.ontop.cli;

import org.junit.Ignore;

/**
 * Created by elem on 27/04/15.
 */
public class QuestOWLMaterializerOutOfMemory {

    //@Ignore("too expensive to run") 
    public void testMaterializerCMD_SeparateFiles (){

        String[] argv = {"-obda", "/Users/elem/Desktop/out-of-memory/npd_vig1uri_obda_scale50.obda",
        "-onto", "/Users/elem/Desktop/out-of-memory/vocabulary.owl",
        "-format", "turtle", "-out", "/Users/elem/Desktop", "--separate-files"};
        QuestOWLMaterializerCMD.main(argv);
    }
}
