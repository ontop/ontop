package it.unibz.krdb.obda.LUBM;


import it.unibz.krdb.obda.model.DataQueryReasoner;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlapi.OBDAOWLReasonerFactory;
import it.unibz.krdb.obda.owlrefplatform.core.DummyOBDAPlatformFactoryImpl;
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
    static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    private OWLOntology ontology;
    private DLLiterOntology dlLiterOntology;
    private String dataDir;
    private String owlfile;

    public TBoxLoader(String dataDir) throws Exception {

        this.dataDir = dataDir;
        this.owlfile = dataDir + "univ-bench.owl";
        ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());
        OWLAPITranslator translator = new OWLAPITranslator();
        dlLiterOntology = translator.translate(ontology);
    }

    public DLLiterOntology loadOnto() throws Exception {

        return dlLiterOntology;
    }


    public DataQueryReasoner loadReasoner(OBDAModel apic, OWLOntologyManager manager) throws Exception {

        OBDAOWLReasonerFactory fac = new DummyOBDAPlatformFactoryImpl();
        fac.setOBDAController(apic);
        return (DataQueryReasoner) fac.createReasoner(manager);


    }


}
