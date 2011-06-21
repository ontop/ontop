package it.unibz.krdb.obda.LUBM;


import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.DataQueryReasoner;
import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.OBDAOWLReformulationPlatformFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;

import java.io.File;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TBoxLoader {

    private static final Logger log = LoggerFactory.getLogger(TBoxLoader.class);
    static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    static OWLOntology ontology;

    public static DLLiterOntology loadOnto(String dataDir) throws Exception {
        String owlfile = dataDir + "univ-bench.owl";
        final long startTime = System.nanoTime();

        ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

        OWLAPITranslator translator = new OWLAPITranslator();

        DLLiterOntology onto = translator.translate(ontology);

        log.info("Loading TBox took: {}", (System.nanoTime() - startTime) * 1.0e-9);

        return onto;

    }

    public static DataQueryReasoner loadReasoner(String dataDir) throws OWLOntologyCreationException {
        String owlfile = dataDir + "univ-bench.owl";
        final long startTime = System.nanoTime();
        ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());
        OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
        OBDAModel apic = obdafac.getOBDAModel();
        
        final String driver = "org.postgresql.Driver";
        final String url = "jdbc:postgresql://localhost/semindex-iswc-5unis:";
        final String username = "obda";
        final String password = "obda09";
        
        DataSource database = obdafac.getJDBCDataSource(url, username, password, driver);
        apic.getDatasourcesController().addDataSource(database);
        
        PrefixManager man = apic.getPrefixManager();
        man.setDefaultNamespace(ontology.getURI().toString());
        man.addUri("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
        man.addUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
        man.addUri("http://www.w3.org/2001/XMLSchema#", "xsd");
        man.addUri("http://www.w3.org/2002/07/owl#", "owl");
        man.addUri("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#", "dllite");


        DataQueryReasoner reasoner;

        ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
        pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, "improved");
        pref.setCurrentValueOf(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS, "false");

        pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, "semantic");
        pref.setCurrentValueOf(ReformulationPlatformPreferences.DATA_LOCATION, "notinmemory");
        pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, "virtual");

        OBDAOWLReformulationPlatformFactoryImpl fac = new OBDAOWLReformulationPlatformFactoryImpl();
        fac.setOBDAController(apic);
        fac.setPreferenceHolder(pref);

        reasoner = (DataQueryReasoner) fac.createReasoner(manager);
        log.info("Creating reasoner took: {}", (System.nanoTime() - startTime) * 1.0e-9);

        return reasoner;
    }

}
