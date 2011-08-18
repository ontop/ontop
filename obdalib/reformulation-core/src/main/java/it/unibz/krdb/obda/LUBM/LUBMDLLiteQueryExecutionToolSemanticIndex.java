package it.unibz.krdb.obda.LUBM;


import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Statement;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.DummyOBDAPlatformFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.OBDAOWLReformulationPlatform;
import it.unibz.krdb.obda.owlrefplatform.core.OBDAOWLReformulationPlatformFactory;
import it.unibz.krdb.obda.queryanswering.QueryControllerEntity;
import it.unibz.krdb.obda.queryanswering.QueryControllerQuery;

import java.io.File;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LUBMDLLiteQueryExecutionToolSemanticIndex {

    public static Logger log = LoggerFactory.getLogger(LUBMDLLiteQueryExecutionToolSemanticIndex.class);


    public static void main(String[] args) {
        String owlfile = "src/test/resources/test/ontologies/scenarios/lubm/univ-bench-dllitea.owl";
        String obdafileBase = "src/test/resources/test/ontologies/scenarios/lubm/univ-bench-dllitea.obda";
        String obdafileReduced = "src/test/resources/test/ontologies/scenarios/lubm/univ-bench-dllitea-semindex-optimal.obda";

        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology;
        try {
            ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

            // Loading the OBDA data (note, the obda file must be in the same
            // folder as the owl file
            OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
            OBDAModel obdamodel = obdafac.getOBDAModel();


            DataManager ioManager = new DataManager(obdamodel);
            ioManager.loadOBDADataFromURI(new File(obdafileBase).toURI(), ontology.getURI(), obdamodel.getPrefixManager());

            DataSource ds = obdafac.getJDBCDataSource(CSVLoader.url, CSVLoader.username, CSVLoader.password, CSVLoader.driver);
            obdamodel.addSource(ds);

            OBDAOWLReformulationPlatformFactory factory = new DummyOBDAPlatformFactoryImpl();

//            factory.setOBDAController(obdamodel);

            OBDAOWLReformulationPlatform reasoner = (OBDAOWLReformulationPlatform) factory.createReasoner(manager);

            reasoner.loadOntologies(manager.getOntologies());

            // Loading a set of configurations for the reasoner and giving them
            // to quonto
            // Properties properties = new Properties();
            // properties.load(new FileInputStream(configFile));
            // QuontoConfiguration config = new QuontoConfiguration(properties);
            // reasoner.setConfiguration(config);

            // One time classification call.
            reasoner.classify();
//
//            // Now we are ready for querying
//
//            for (QueryControllerEntity entity : obdamodel.getQueryController().getElements()) {
//                if (!(entity instanceof QueryControllerQuery)) {
//                    continue;
//                }
//                QueryControllerQuery query = (QueryControllerQuery) entity;
//                String sparqlquery = query.getQuery();
//                String id = query.getID();
//                log.info("##################  Rewriting query: {}", id);
//
//                Statement st = reasoner.getStatement();
//
//                long start = System.currentTimeMillis();
//                String rewriting = st.getRewriting(sparqlquery);
//                long end = System.currentTimeMillis();
//                log.info("Total time for rewriting: {}", end - start);
//
//            }
//            ioManager.loadOBDADataFromURI(new File(obdafileReduced).toURI(), ontology.getURI(), obdamodel.getPrefixManager());
//            for (QueryControllerEntity entity : obdamodel.getQueryController().getElements()) {
//                if (!(entity instanceof QueryControllerQuery)) {
//                    continue;
//                }
//                QueryControllerQuery query = (QueryControllerQuery) entity;
//                String sparqlquery = query.getQuery();
//                String id = query.getID();
//                log.info("##################  Unfolding query: {}", id);
//
//                Statement st = reasoner.getStatement();
//
//                long start = System.currentTimeMillis();
//                String rewriting = st.getUnfolding(sparqlquery);
//                long end = System.currentTimeMillis();
//                log.info("Total time for unfolding: {}", end - start);
//
//            }

            // Now we are ready for querying

            for (QueryControllerEntity entity : obdamodel.getQueryController().getElements()) {
                if (!(entity instanceof QueryControllerQuery)) {
                    continue;
                }
                QueryControllerQuery query = (QueryControllerQuery) entity;
                String sparqlquery = query.getQuery();
                String id = query.getID();
                log.info("##################  Rewriting query: {}", id);

                Statement st = reasoner.getStatement();

                long start = System.currentTimeMillis();
                String rewriting = st.getRewriting(sparqlquery);
                long end = System.currentTimeMillis();
                log.info("Total time for rewriting: {}", end - start);

            }
            ioManager.loadOBDADataFromURI(new File(obdafileReduced).toURI(), ontology.getURI(), obdamodel.getPrefixManager());
            for (QueryControllerEntity entity : obdamodel.getQueryController().getElements()) {
                if (!(entity instanceof QueryControllerQuery)) {
                    continue;
                }
                QueryControllerQuery query = (QueryControllerQuery) entity;
                String sparqlquery = query.getQuery();
                String id = query.getID();
                log.info("##################  Unfolding query: {}", id);

                Statement st = reasoner.getStatement();

                long start = System.currentTimeMillis();
                String rewriting = st.getUnfolding(sparqlquery);
                long end = System.currentTimeMillis();
                log.info("Total time for unfolding: {}", end - start);

            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


}
