package it.unibz.krdb.obda.LUBM;


import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TBoxLoader {

    private static final Logger log = LoggerFactory.getLogger(TBoxLoader.class);

//    public String owlfile = "src/test/resources/univ-bench.owl";

    public static DLLiterOntology loadOnto(String dataDir) throws Exception {
        String owlfile = dataDir + "univ-bench.owl";
        final long startTime = System.nanoTime();
        final long endTime;


        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        final OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

        OWLAPITranslator translator = new OWLAPITranslator();

        DLLiterOntology onto = translator.translate(ontology);
        endTime = System.nanoTime();
        final long duration = endTime - startTime;

        log.info("Loading TBox took: {}", duration * 1.0e-9);

        return onto;

    }

}
