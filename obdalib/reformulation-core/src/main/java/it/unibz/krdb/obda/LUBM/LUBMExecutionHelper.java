package it.unibz.krdb.obda.LUBM;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlapi.OBDAOWLReasonerFactory;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.GraphGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.core.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.core.QuestTechniqueWrapper;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeWitnessReformulator;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LUBMExecutionHelper {

	public static Logger		log				= LoggerFactory.getLogger(LUBMExecutionHelper.class);
	private static final int	ABOX_FILE_COUNT	= 5;

	public LUBMExecutionHelper() {

	}

	public void run(String owlfile, String obdafile, String testmode, String aboxmode, String rewritingmode, boolean graph)
			throws Exception {
//		GraphGenerator.debugInfoDump = graph;

		// abox mode: classic, semindex

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology;

		if (testmode.equals("execute")) {
			String aboxfmt = new File(owlfile).getParent() + "/University0_%s.owl";
			String aboxpath;
			for (int i = 0; i < ABOX_FILE_COUNT; ++i) {
				aboxpath = String.format(aboxfmt, i);
				manager.loadOntologyFromPhysicalURI((new File(aboxpath).toURI()));
			}
		}

		ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

		// Loading the OBDA data (note, the obda file must be in the same
		// folder as the owl file
		OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdamodel = obdafac.getOBDAModel();

		/* Preparing a dummy datsource for this test */
		DataManager ioManager = new DataManager(obdamodel);
		ioManager.loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getURI(), obdamodel.getPrefixManager());
		List<OBDADataSource> sources = obdamodel.getSources();
		for (OBDADataSource source : sources) {
			obdamodel.removeSource(source.getSourceID());
		}

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump";
		String username = "sa";
		String password = "";
		Connection connection;

		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		connection = JDBCConnectionManager.getJDBCConnectionManager().getConnection(null);

		obdamodel.addSource(source);

		// Creating a new instance of a Quest reasoner

		OBDAOWLReasonerFactory factory = new QuestOWLFactory();

		ReformulationPlatformPreferences p = new ReformulationPlatformPreferences();

		if (aboxmode.equals("semindex")) {
			p.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		} else if (aboxmode.equals("classic")) {
			p.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.DIRECT);

		} else {
			throw new Exception("Unsupported ABox mode. specify either \"classic\" or \"semindex\"");
		}

		p.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);

//		factory.setOBDAController(obdamodel);
		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(manager);
		reasoner.setPreferences(p);
		reasoner.loadOntologies(manager.getOntologies());
		reasoner.loadOBDAModel(obdamodel);

		// One time classification call.
		reasoner.classify();

		if (rewritingmode.equals("treewitness")) {

			TreeWitnessReformulator ref = null;
			if (aboxmode.equals("classic")) {
				ref = new TreeWitnessReformulator();
				ref.setTBox(reasoner.getOntology());
				
			} else if (aboxmode.equals("semindex")) {
				ref = new TreeWitnessReformulator();
				ref.setTBox(reasoner.getReducedOntology());
				ref.setCBox(reasoner.getABoxDependencies());
				
			}
			((QuestTechniqueWrapper) reasoner.getTechniqueWrapper()).setRewriter(ref);
		}

		// Now we are ready for querying

		for (QueryControllerEntity entity : obdamodel.getQueryController().getElements()) {
			System.gc();
			System.runFinalization();
			Thread.currentThread().yield();
			
			if (!(entity instanceof QueryControllerQuery)) {
				continue;
			}
			QueryControllerQuery query = (QueryControllerQuery) entity;
			String sparqlquery = query.getQuery();
			String id = query.getID();
			log.info("##################  Testing query: {}", id);

			OBDAStatement st = reasoner.getStatement();

			long start = System.currentTimeMillis();

			if (testmode.equals("rewrite")) {
				CountDownLatch latch = new CountDownLatch(1);
				ReformulationThread refThread = new ReformulationThread(latch, st, sparqlquery);
				refThread.start();

				log.debug("MAX MEM: {}  FREE MEMB: {}", Runtime.getRuntime().maxMemory(), Runtime.getRuntime().freeMemory());

				log.debug("Waiting for reformulation thread to finish or time out...");
				boolean loop = true;
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				while (loop) {
					latch.await(1000, TimeUnit.MILLISECONDS);
					if (latch.getCount() == 0) {
						loop = false;
					} else {
						/* checking if memory is over the limit */
						double used = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
						double maxmem = Runtime.getRuntime().maxMemory();
						double percentused = used / maxmem;
						// log.debug("Checking: {}", percentused);
						if (percentused > .90) {
							/*
							 * More than 75% is being used, dangerous, abort
							 * before swaping ocurss
							 */
							loop = false;
						}
					}
				}
				log.debug("Rewriting finished by itself");
				String rewriting = refThread.getResult();
				
				if (rewriting == null) {
					log.debug("ABORTED");
					/*
					 * The rewriting has not finished, but we timed out,
					 * canceling the rewriting thread
					 */
					try {
						refThread.stop();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
					Thread.currentThread().sleep(1000);
					
				} else {
					/*
					 * The rewriting finished normally nothing to do anymore
					 */

				}

			} else if (testmode.equals("unfold")) {
				String unfolding = st.getUnfolding(sparqlquery, false);
			} else if (testmode.equals("execute")) {
				OBDAResultSet res = st.executeqlquery);
				log.debug("Result size: {}", res.getFetchSize());
			} else {
				System.err.println("Unsupported mode. specify either \"rewrite\" or \"unfold\" or \"execute\"");
				throw new Exception("Unsupported mode. specify either \"rewrite\" or \"unfold\"");
			}
			long end = System.currentTimeMillis();
			log.info("Total time elapsed for this query: {}", end - start);

		}
		log.info("All queries have been evaluated");
	}

	public static void main(String args[]) {
		try {

			// 1 is ontology
			// 2 is OBDA file

			// 3 mode: "rewrite", "unfold"

			String testmode = args[0];
			String aboxmode = args[1];
			String rewritingmode = args[2];
			String owlfile = args[3];
			String obdafile = args[4];

			boolean graph = false;

			if (args.length == 6) {
				if (args[5].equals("graph")) {
					graph = true;
				} else {
					throw new Exception("Unrecognized option: " + args[4]);
				}
			}
			LUBMExecutionHelper helper = new LUBMExecutionHelper();
			helper.run(owlfile, obdafile, testmode, aboxmode, rewritingmode, graph);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.info("Usage: LUBMExecutionHelper testmode aboxmode owlfile obdafile");
			log.info("Where testmode = rewrite or unfold");
			log.info("      aboxmode = classic or semindex");
			log.info("      owlfile is the location of the owl file to use for rewritings");
			log.info("      obdafile is the location of the obda file to that contains the test queries");
		}

	}

	public class ReformulationThread extends Thread {

		final private CountDownLatch	latch;
		final private OBDAStatement			st;
		final private String			query;
		private String					reformulation	= null;

		public ReformulationThread(CountDownLatch latch, OBDAStatement st, String query) {
			this.query = query;
			this.latch = latch;
			this.st = st;
			this.setPriority(MIN_PRIORITY);
		}

		@Override
		public void run() {
			try {
				log.debug("Reformulation thread started...");
				reformulation = st.getRewriting(query);
			} catch (Exception e) {
				log.error(e.getMessage());
			} finally {
				latch.countDown();
			}
			log.debug("Reformulation thread finished");
		}

		public String getResult() {
			return reformulation;
		}

	}
}
