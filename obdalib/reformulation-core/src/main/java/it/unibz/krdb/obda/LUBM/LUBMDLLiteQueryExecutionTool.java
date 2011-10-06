package it.unibz.krdb.obda.LUBM;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi.OBDAOWLReasonerFactory;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.core.QuestOWLFactory;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import java.io.File;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LUBMDLLiteQueryExecutionTool {

	public static Logger	log	= LoggerFactory.getLogger(LUBMDLLiteQueryExecutionTool.class);

	public static void main(String args[]) {
		String owlfile = "src/test/resources/test/ontologies/scenarios/lubm/univ-bench-dllitea.owl";
		String obdafile = "src/test/resources/test/ontologies/scenarios/lubm/univ-bench-dllitea.obda";

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
			ioManager.loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getURI(), obdamodel.getPrefixManager());

			// Creating a new instance of a Quest reasoner
			OBDAOWLReasonerFactory factory = new QuestOWLFactory();

			ReformulationPlatformPreferences p = new ReformulationPlatformPreferences();
			p.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, "material");
			p.setCurrentValueOf(ReformulationPlatformPreferences.DATA_LOCATION, "inmemory");

//			factory.setOBDAController(obdamodel);
			factory.setPreferenceHolder(p);

			QuestOWL reasoner = (QuestOWL) factory.createReasoner(manager);

			reasoner.loadOntologies(manager.getOntologies());

			// Loading a set of configurations for the reasoner and giving them
			// Properties properties = new Properties();
			// properties.load(new FileInputStream(configFile));

			// One time classification call.
			reasoner.classify();

			// Now we are ready for querying

			for (QueryControllerEntity entity : obdamodel.getQueryController().getElements()) {
				if (!(entity instanceof QueryControllerQuery)) {
					continue;
				}
				QueryControllerQuery query = (QueryControllerQuery)entity;
				String sparqlquery = query.getQuery();
				String id = query.getID();
				log.info("##################  Executing query: {}", id);
				
				
				OBDAStatement st = reasoner.getStatement();
				
				long start = System.currentTimeMillis();				
				String rewriting = st.getRewriting(sparqlquery);
				long end = System.currentTimeMillis();
				log.info("Total time for rewriting: {}", end-start);
				
				
				
				
				
				
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}
}
