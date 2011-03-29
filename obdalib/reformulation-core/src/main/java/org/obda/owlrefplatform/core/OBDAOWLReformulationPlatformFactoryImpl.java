package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.owlapi.ReformulationPlatformPreferences;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.obda.owlrefplatform.core.abox.ABoxToDBDumper;
import org.obda.owlrefplatform.core.abox.ABoxToDBDumper;
import org.obda.owlrefplatform.core.ontology.DLLiterOntology;
import org.obda.owlrefplatform.core.ontology.imp.DLLiterOntologyImpl;
import org.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;
import org.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import org.obda.owlrefplatform.core.queryevaluation.JDBCEngine;
import org.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import org.obda.owlrefplatform.core.reformulation.DLRPerfectReformulator;
import org.obda.owlrefplatform.core.reformulation.QueryRewriter;
import org.obda.owlrefplatform.core.reformulation.TreeRedReformulator;
import org.obda.owlrefplatform.core.srcquerygeneration.ComplexMappingSQLGenerator;
import org.obda.owlrefplatform.core.srcquerygeneration.SimpleDirectQueryGenrator;
import org.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import org.obda.owlrefplatform.core.unfolding.ComplexMappingUnfolder;
import org.obda.owlrefplatform.core.unfolding.DirectMappingUnfolder;
import org.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import org.obda.owlrefplatform.core.viewmanager.MappingViewManager;
import org.obda.owlrefplatform.exception.OBDAOWLReformulaionPlatformFactoryException;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the factory for creating refomultions platform reasoner
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public class OBDAOWLReformulationPlatformFactoryImpl implements OBDAOWLReformulationPlatformFactory {

	protected APIController		apic			= null;
	private ReformulationPlatformPreferences preference = null;
	private String				id;
	private String				name;
	private OWLOntologyManager	owlOntologyManager;
	private boolean				useInMemoryDB	= false;
	private boolean				createMappings	= false;
	private String				unfoldingMode	= null;
	private String				reformulationTechnique = "";

	private Logger				log				= LoggerFactory.getLogger(OBDAOWLReformulationPlatformFactoryImpl.class);

	/**
	 * Sets up some prerequirements in order to create the reasoner
	 * 
	 * @param manager
	 *            the owl ontology manager
	 * @param id
	 *            the reasoner id
	 * @param name
	 *            the reasoner name
	 */
	public void setup(OWLOntologyManager manager, String id, String name) {
		this.id = id;
		this.name = name;
		this.owlOntologyManager = manager;
	}

	/**
	 * Return the current OWLOntologyManager
	 * 
	 * @return the current OWLOntologyManager
	 */
	public OWLOntologyManager getOWLOntologyManager() {
		return owlOntologyManager;
	}

	/**
	 * Returns the current reasoner id
	 * 
	 * @return the current reasoner id
	 */
	public String getReasonerId() {
		return id;
	}
	
	@Override
	public void setOBDAController(APIController apic) {
		this.apic = apic;
		ABoxToDBDumper.getInstance().setAPIController(apic);
	}

	@Override
	public void setPreferenceHolder(ReformulationPlatformPreferences preference) {
		this.preference = preference;
	}

	/**
	 * Creates a new reformulation platform reasoner.
	 */
	public OWLReasoner createReasoner(OWLOntologyManager manager) {
		log.debug("Creating a new instance of the BolzanoTechniqueWrapper");
		
		if (apic == null) {
			throw new NullPointerException(
					"The APIController for this OBDAOWLReformulationFactory " +
					"has not been set. Call setAPIController(APIController " +
					"apic) before instantiating a reasoner.");
		}
		
		if (preference == null) {
			throw new NullPointerException(
					"The preferences for this OBDAOWLReformulationFactory " +
					"has not been set. Call setPreferenceHolder(" +
					"ReformulationPlatformPreferences preference) before " +
					"instantiating a reasoner.");
		}
		
		String useMem = (String) preference.getCurrentValue(ReformulationPlatformPreferences.USE_INMEMORY_DB);
		unfoldingMode = (String) preference.getCurrentValue(ReformulationPlatformPreferences.UNFOLDING_MECHANMISM);
		String createMap = (String) preference.getCurrentValue(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS);
		reformulationTechnique = (String) preference.getCurrentValue(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE);
		
		log.debug("Parameters: ");
		log.debug("{}={}", ReformulationPlatformPreferences.USE_INMEMORY_DB, (String) preference
				.getCurrentValue(ReformulationPlatformPreferences.USE_INMEMORY_DB));
		log.debug("{}={}", ReformulationPlatformPreferences.UNFOLDING_MECHANMISM, (String) preference
				.getCurrentValue(ReformulationPlatformPreferences.UNFOLDING_MECHANMISM));
		log.debug("{}={}", ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS, (String) preference
				.getCurrentValue(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS));

		createMappings = createMap.equals("true");
		useInMemoryDB = useMem.equals("true");

		// TODO OBDAOWLReformulationPlattformFactory add here code to validate
		// all the known parameters

		OBDAOWLReformulationPlatform reasoner;
		try {
			TechniqueWrapper wra = null;
			if (unfoldingMode.equals("direct")) {
				wra = createTechniqueWrapperForSimpleUnfoldig();
			} else if (unfoldingMode.equals("complex")) {
				wra = createTechniqueWrapperForComplexMappingUnfoldig();
			} else {
				throw new IllegalArgumentException("The value for the parameter " + ReformulationPlatformPreferences.UNFOLDING_MECHANMISM
						+ " is invalid. Use: direct or complex");
			}
			reasoner = new OBDAOWLReformulationPlatform(apic, owlOntologyManager, wra);
			return reasoner;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			RuntimeException e2 = new RuntimeException(e.getMessage());
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		}

	}

	/**
	 * used to create a reasoner which can handle mappings
	 * 
	 * @return a new reformulation plataform reasoner
	 * @throws Exception
	 */
	private TechniqueWrapper createTechniqueWrapperForComplexMappingUnfoldig() throws Exception {

		// TODO OBDAOWLReformlationPlattformFActoryImpl. The
		// createteTechniqueWrapperForComplexMappingUnfolding and for simple
		// unfolding are almost mirrors to each other. Both methods should be
		// combined into a single method to avoid repeating code.

		log.debug("Creating an instance of the technique wrapper with complex mapping unfolder");
		
		log.debug("Translating ontologies");
		Set<OWLOntology> ontologies = owlOntologyManager.getOntologies();
		OWLAPITranslator translator = new OWLAPITranslator();
		URI uri = null;
		if (ontologies.size() > 0) {
			uri = ontologies.iterator().next().getURI();
		}
		DLLiterOntology ontology = new DLLiterOntologyImpl(uri);

		Iterator<OWLOntology> it = ontologies.iterator();
		while (it.hasNext()) {
			OWLOntology o = it.next();
			DLLiterOntology aux = translator.translate(o);
			ontology.addAssertions(aux.getAssertions());
		}
		
		DataSource ds = null;
		EvaluationEngine eng = null;
		
		if (!useInMemoryDB) {
			log.debug("Using a persistent database");
			
			Collection<DataSource> sources = apic.getDatasourcesController().getAllSources().values();
			if (sources == null || sources.size() == 0) {
				throw new Exception("No datasource selected");
			} else if (sources.size() > 1) {
				throw new Exception("Currently the reasoner can only handle one datasource");
			} else {
				ds = sources.iterator().next();
			}
//			log.debug("Using datasource: {}", ds.getSourceID().toASCIIString());
			
			eng = new JDBCEngine(ds);
		} else {
			log.debug("Using in an memory database");
			String driver = "org.h2.Driver";
			String url = "jdbc:h2:mem:";
			String dbname = "aboxdump";
			String username = "sa";
			String password = "";
			Connection connection = null;

			DataSource source = new DataSource(URI.create("http://www.obda.org/ABOXDUMP"));
			source.setParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER, driver);
			source.setParameter(RDBMSsourceParameterConstants.DATABASE_NAME, dbname);
			source.setParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD, password);
			source.setParameter(RDBMSsourceParameterConstants.DATABASE_URL, url);
			source.setParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME, username);
			source.setParameter(RDBMSsourceParameterConstants.IS_IN_MEMORY, "true");
			source.setParameter(RDBMSsourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

			apic.getDatasourcesController().addDataSource(source);
			apic.getDatasourcesController().setCurrentDataSource(source.getSourceID());

//			Class.forName(driver);
			connection = DriverManager.getConnection(url + dbname, username, password);
			String[] types = { "TABLE" };
			ResultSet set = connection.getMetaData().getTables(null, null, "%", types);
			Vector<String> drops = new Vector<String>();
			while (set.next()) {
				String table = set.getString(3);
				drops.add("DROP TABLE " + table);
			}
			set.close();
			Iterator<String> drit = drops.iterator();
			Statement st = connection.createStatement();
			while (drit.hasNext()) {
				st.executeUpdate(drit.next());
			}
			if (connection != null) {
				eng = new JDBCEngine(connection);
				try {
					ABoxToDBDumper.getInstance().materialize(ontologies, connection, source.getSourceID(), createMappings);
				} catch (SQLException e) {
					throw new OBDAOWLReformulaionPlatformFactoryException(e);
				}
			} else {
				try {
					throw new Exception("Could not establish connection do in memory DB");
				} catch (Exception e) {
					throw new OBDAOWLReformulaionPlatformFactoryException(e);
				}
			}
			ds = source;
		}
		// ds = apic.getDatasourcesController().getCurrentDataSource();
		
		QueryRewriter rew = null;
		if (reformulationTechnique.equals("dlr")) {
			rew = new DLRPerfectReformulator(ontology.getAssertions());
		}
		else if (reformulationTechnique.equals("improved")) {
			rew = new TreeRedReformulator(ontology.getAssertions());
		}
		
		List<OBDAMappingAxiom> mappings = apic.getMappingController().getMappings(ds.getSourceID());
		
		
		MappingViewManager viewMan = new MappingViewManager(mappings);
		
		UnfoldingMechanism unfMech = new ComplexMappingUnfolder(mappings, viewMan);
		
		JDBCUtility util = new JDBCUtility(ds.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER));
		
		SourceQueryGenerator gen = new ComplexMappingSQLGenerator(ontology, viewMan, util);
		
		log.debug("Done setting up the technique wrapper");

		return new BolzanoTechniqueWrapper(unfMech, rew, gen, eng, apic);
	}

	/**
	 * Used to create a direct mapping reasoner
	 * 
	 * @return a new reformulation plataform reasoner
	 * @throws Exception
	 */
	private TechniqueWrapper createTechniqueWrapperForSimpleUnfoldig() throws Exception {
		log.debug("Instatiating a a technique wrapper with simple unfoldings.");

		UnfoldingMechanism unf = new DirectMappingUnfolder();
		
		Set<OWLOntology> ontologies = owlOntologyManager.getOntologies();
		OWLAPITranslator translator = new OWLAPITranslator();
		URI uri = null;
		if (ontologies.size() > 0) {
			uri = ontologies.iterator().next().getURI();
		}
		DLLiterOntology ontology = new DLLiterOntologyImpl(uri);

		Iterator<OWLOntology> it = ontologies.iterator();
		log.debug("Translating the ontology");
		Set<URI> uris = new HashSet<URI>();
		while (it.hasNext()) {
			OWLOntology o = it.next();
			uris.add(o.getURI());
			DLLiterOntology aux = translator.translate(o);
			ontology.addAssertions(aux.getAssertions());
		}

		QueryRewriter rew = null;
		if (reformulationTechnique.equals("dlr")) {
			rew = new DLRPerfectReformulator(ontology.getAssertions());
		}
		else if (reformulationTechnique.equals("improved")) {
			rew = new TreeRedReformulator(ontology.getAssertions());
		}
		
		log.debug("Instantiating a SimpleDirectQueryGenrator");
		SourceQueryGenerator gen = new SimpleDirectQueryGenrator(apic.getIOManager().getPrefixManager(), ontology, uris);
		
		EvaluationEngine eng = null;
		if (!useInMemoryDB) {
			log.debug("Using a persistent RDBMS");
			Iterator<DataSource> dit = apic.getDatasourcesController().getAllSources().values().iterator();
			DataSource ds = dit.next();  // Take the first data source on the list.
			
			if (ds == null) {
				throw new Exception("No suitable data source found! Please do a manual abox dump or select the in memory mode");
			}
			eng = new JDBCEngine(ds);
		} else {

			
			//TODO All the database management code should be put in a separate method or class
			
			log.debug("Setting up an in H2 in-memory database to store the ABox");
			String driver = "org.h2.Driver";
			String url = "jdbc:h2:mem:";
			String dbname = "aboxdump";
			String username = "sa";
			String password = "";
			Connection connection = null;

//			Class.forName(driver);
			connection = DriverManager.getConnection(url + dbname, username, password);
			String[] types = { "TABLE" };
			ResultSet set = connection.getMetaData().getTables(null, null, "%", types);
			Vector<String> drops = new Vector<String>();
			while (set.next()) {
				String table = set.getString(3);
				drops.add("DROP TABLE " + table);
			}
			set.close();
			Iterator<String> drit = drops.iterator();
			Statement st = connection.createStatement();
			while (drit.hasNext()) {
				st.executeUpdate(drit.next());
			}

			//TODO this code will never do anything
			if (connection == null)
				throw new OBDAOWLReformulaionPlatformFactoryException(new Exception("Could not establish connection do in memory DB"));

			eng = new JDBCEngine(connection);

			log.debug("Inserting ABox data from the ontology into the database");
			try {
				ABoxToDBDumper.getInstance().materialize(ontologies, connection, null, false);
			} catch (SQLException e) {
				throw new OBDAOWLReformulaionPlatformFactoryException(e);
			}

		}
		
		log.debug("Done setting up the technique wrapper");
		
		return new BolzanoTechniqueWrapper(unf, rew, gen, eng, apic);
	}

	public String getReasonerName() {
		return name;
	}

	public void initialise() throws Exception {

	}

	public void dispose() throws Exception {

	}

	/**
	 * Returns the current api controller
	 * 
	 * @return the current api controller
	 */
	public APIController getApiController() {

		if (apic == null) {
			JOptionPane.showMessageDialog(null, "Creation of reasoner failed. Look up the logfile for more information", "FAILURE",
					JOptionPane.ERROR_MESSAGE);
			throw new NullPointerException(
					"The APIController for this factory has not been set. use setAPIController(APIController apic) to set it and try again.");
		} else {
			return apic;
		}
	}
}
