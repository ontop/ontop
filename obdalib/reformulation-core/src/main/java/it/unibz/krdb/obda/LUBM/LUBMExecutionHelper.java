package it.unibz.krdb.obda.LUBM;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Statement;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.DummyOBDAPlatformFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.GraphGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.OBDAOWLReformulationPlatform;
import it.unibz.krdb.obda.owlrefplatform.core.OBDAOWLReformulationPlatformFactory;
import it.unibz.krdb.obda.owlrefplatform.core.OBDAOWLReformulationPlatformFactoryImpl;
import it.unibz.krdb.obda.queryanswering.QueryControllerEntity;
import it.unibz.krdb.obda.queryanswering.QueryControllerQuery;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.util.List;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LUBMExecutionHelper {

	public static Logger	log	= LoggerFactory.getLogger(LUBMExecutionHelper.class);

	public static void main(String args[]) {
		try {

			String owlfile = args[2];
			String obdafile = args[3];

			// 1 is ontology
			// 2 is OBDA file

			// 3 mode: "rewrite", "unfold"

			String testmode = args[0];
			String aboxmode = args[1];
			
			boolean graph = false;
			
			if (args.length == 5) {
				if (args[4].equals("graph")) {
					graph = true;
				} else {
					throw new Exception("Unrecognized option: " + args[4]);
				}
			}
			GraphGenerator.debugInfoDump = graph;

			// abox mode: classic, semindex

			// Loading the OWL file
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology;

			ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

			// Loading the OBDA data (note, the obda file must be in the same
			// folder as the owl file
			OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
			OBDAModel obdamodel = obdafac.getOBDAModel();

			
			/* Preparing a dummy datsource for this test */
			DataManager ioManager = new DataManager(obdamodel);
			ioManager.loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getURI(), obdamodel.getPrefixManager());
			List<DataSource> sources =obdamodel.getDatasourcesController().getAllSources();
			for (DataSource source: sources) {
				obdamodel.getDatasourcesController().removeDataSource(source.getSourceID());
			}
			
			String driver = "org.h2.Driver";
            String url = "jdbc:h2:mem:aboxdump";
            String username = "sa";
            String password = "";
            Connection connection;

            OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
            DataSource source = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP"));
            source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
            source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
            source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
            source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
            source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
            source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

            connection = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);
            
            obdamodel.getDatasourcesController().addDataSource(source);


			// Creating a new instance of a Quest reasoner

			OBDAOWLReformulationPlatformFactory factory = null;

			if (aboxmode.equals("classic")) {
				factory = new OBDAOWLReformulationPlatformFactoryImpl();
			} else if (aboxmode.equals("semindex")) {
				factory = new DummyOBDAPlatformFactoryImpl();

			} else {
				System.err.println("Unsupported ABox mode. specify either \"classic\" or \"semindex\"");
				throw new Exception("Unsupported ABox mode. specify either \"classic\" or \"semindex\"");
			}

			ReformulationPlatformPreferences p = new ReformulationPlatformPreferences();
			p.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, "material");
			p.setCurrentValueOf(ReformulationPlatformPreferences.DATA_LOCATION, "inmemory");

			factory.setOBDAController(obdamodel);
			factory.setPreferenceHolder(p);

			OBDAOWLReformulationPlatform reasoner = (OBDAOWLReformulationPlatform) factory.createReasoner(manager);

			reasoner.loadOntologies(manager.getOntologies());

			// One time classification call.
			reasoner.classify();

			// Now we are ready for querying

			for (QueryControllerEntity entity : obdamodel.getQueryController().getElements()) {
				if (!(entity instanceof QueryControllerQuery)) {
					continue;
				}
				QueryControllerQuery query = (QueryControllerQuery) entity;
				String sparqlquery = query.getQuery();
				String id = query.getID();
				log.info("##################  Testing query: {}", id);

				Statement st = reasoner.getStatement();

				long start = System.currentTimeMillis();

				if (testmode.equals("rewrite")) {
					String rewriting = st.getRewriting(sparqlquery);
				} else if (testmode.equals("unfold")) {
					String unfolding = st.getUnfolding(sparqlquery, false);
				} else {
					System.err.println("Unsupported mode. specify either \"rewrite\" or \"unfold\"");
					throw new Exception("Unsupported mode. specify either \"rewrite\" or \"unfold\"");
				}
				long end = System.currentTimeMillis();
				log.info("Total time elapsed for this query: {}", end - start);

			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.info("Usage: LUBMExecutionHelper testmode aboxmode owlfile obdafile");
			log.info("Where testmode = rewrite or unfold");
			log.info("      aboxmode = classic or semindex");
			log.info("      owlfile is the location of the owl file to use for rewritings");
			log.info("      obdafile is the location of the obda file to that contains the test queries");
		}

	}
}
