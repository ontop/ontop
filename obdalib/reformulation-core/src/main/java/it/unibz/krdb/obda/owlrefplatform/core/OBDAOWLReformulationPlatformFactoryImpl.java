package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.MappingController;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.abox.ABoxSerializer;
import it.unibz.krdb.obda.owlrefplatform.core.abox.ABoxToDBDumper;
import it.unibz.krdb.obda.owlrefplatform.core.abox.AboxDumpException;
import it.unibz.krdb.obda.owlrefplatform.core.abox.AboxFromDBLoader;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DirectMappingGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.MappingValidator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexMappingGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticReduction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterOntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.DLRPerfectReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeRedReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.ComplexMappingSQLGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SimpleDirectQueryGenrator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.ComplexMappingUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DirectMappingUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.MappingViewManager;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the factory for creating reformulation's platform reasoner
 *
 */

public class OBDAOWLReformulationPlatformFactoryImpl implements OBDAOWLReformulationPlatformFactory {

    private OBDAModel apic;
    private ReformulationPlatformPreferences preferences = null;
    private String id;
    private String name;
    private OWLOntologyManager owlOntologyManager;


    private final Logger log = LoggerFactory.getLogger(OBDAOWLReformulationPlatformFactoryImpl.class);

    /**
     * Sets up some prerequirements in order to create the reasoner
     *
     * @param manager the owl ontology manager
     * @param id      the reasoner id
     * @param name    the reasoner name
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
    public void setOBDAController(OBDAModel apic) {
        this.apic = apic;
    }

    @Override
    public void setPreferenceHolder(ReformulationPlatformPreferences preference) {
        this.preferences = preference;
    }


    /**
     * Creates a new reformulation platform reasoner.
     */
    @Override
    public OWLReasoner createReasoner(OWLOntologyManager manager) {

        if (apic == null) {
            throw new NullPointerException("APIController not set");
        }
        if (preferences == null) {
            throw new NullPointerException("ReformulationPlatformPreferences not set");
        }

        //String useMem = (String)
        String reformulationTechnique = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE);
        boolean useInMemoryDB = preferences.getCurrentValue(ReformulationPlatformPreferences.DATA_LOCATION).equals("inmemory");
        String unfoldingMode = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.ABOX_MODE);
        String dbType = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.DBTYPE);
        boolean createMappings = preferences.getCurrentValue(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS).equals("true");
        log.debug("Initializing Quest query answering engine...");
        log.debug("Active preferences:");
        log.debug("{} = {}", ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, reformulationTechnique);
        log.debug("{} = {}", ReformulationPlatformPreferences.DATA_LOCATION, useInMemoryDB);
        log.debug("{} = {}", ReformulationPlatformPreferences.ABOX_MODE, unfoldingMode);
        log.debug("{} = {}", ReformulationPlatformPreferences.DBTYPE, dbType);
        log.debug("{} = {}", ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS, createMappings);



        QueryRewriter rewriter;
        TechniqueWrapper techniqueWrapper;
        UnfoldingMechanism unfMech = null;
        SourceQueryGenerator gen = null;
        DataSource ds;
        EvaluationEngine eval_engine;
        DAG dag = null;

        try {
            Set<OWLOntology> ontologies = manager.getOntologies();
            URI uri = null;
            if (ontologies.size() > 0) {  // XXX Always take the first URI in the list?
                uri = ontologies.iterator().next().getURI();
            }
            DLLiterOntology ontology = new DLLiterOntologyImpl(uri);

            log.debug("Translating ontologies");
            OWLAPITranslator translator = new OWLAPITranslator();
            Set<URI> uris = new HashSet<URI>();
            for (OWLOntology onto : ontologies) {
                uris.add(onto.getURI());
                DLLiterOntology aux = translator.translate(onto);
                ontology.addAssertions(aux.getAssertions());
            }

            if (useInMemoryDB && ("material".equals(unfoldingMode)||createMappings)) {
                log.debug("Using in an memory database");
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

//                apic.getDatasourcesController().addDataSource(source);
//                apic.getDatasourcesController().setCurrentDataSource(source.getSourceID());
                ds = source;
                connection = JDBCConnectionManager.getJDBCConnectionManager().getConnection(ds);
                if (dbType.equals("semantic")) {
                    //perform semantic import
                    dag = new DAG(ontologies);
                    ABoxSerializer.recreate_tables(connection);
                    ABoxSerializer.ABOX2DB(ontologies, dag, connection);
                } else if (dbType.equals("direct")) {
                    //perform direct import
                    String[] types = {"TABLE"};

                    ResultSet set = connection.getMetaData().getTables(null, null, "%", types);
                    Vector<String> drops = new Vector<String>();
                    while (set.next()) {
                        String table = set.getString(3);
                        drops.add("DROP TABLE " + table);
                    }
                    set.close();

                    Statement st = connection.createStatement();
                    for (String drop_table : drops) {
                        st.executeUpdate(drop_table);
                    }
                    ABoxToDBDumper dumper;
                    try {
                        dumper = new ABoxToDBDumper(source);
                        dumper.materialize(ontologies, false);
                    } catch (AboxDumpException e) {
                        throw new Exception(e);
                    }
                    if (createMappings) {
                        DirectMappingGenerator mapGen = new DirectMappingGenerator();
                        Set<OBDAMappingAxiom> mappings = mapGen.getMappings(ontologies, dumper.getMapper());
                        Iterator<OBDAMappingAxiom> it = mappings.iterator();
                        MappingController mapCon = apic.getMappingController();
                        while (it.hasNext()) {
                            mapCon.insertMapping(ds.getSourceID(), it.next());
                        }
                    }
                } else {
                    throw new Exception(dbType + " is unknown or not yet supported Data Base type. Currently only the direct db type is supported");
                }
                eval_engine = new JDBCEngine(connection);

            } else {
                log.debug("Using a persistent database");

                Collection<DataSource> sources = apic.getDatasourcesController().getAllSources();
                if (sources == null || sources.size() == 0) {
                    throw new Exception("No datasource has been defined");
                } else if (sources.size() > 1) {
                    throw new Exception("Currently the reasoner can only handle one datasource");
                } else {
                    ds = sources.iterator().next();
                }
                eval_engine = new JDBCEngine(ds);
            }
            List<Assertion> onto;
            if (dbType.equals("semantic")) {

                // Reachability DAGs
                TDAG tdag = new TDAG(dag);
                SDAG sdag = new SDAG(tdag);

                SemanticReduction reducer = new SemanticReduction(dag, tdag, sdag);
                onto = reducer.reduce();
            } else {
                onto = ontology.getAssertions();
            }

            if ("dlr".equals(reformulationTechnique)) {
                rewriter = new DLRPerfectReformulator(onto);
            } else if ("improved".equals(reformulationTechnique)) {
                rewriter = new TreeRedReformulator(onto);
            } else {
                throw new IllegalArgumentException("Invalid value for argument: " + ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE);
            }

            if ("virtual".equals(unfoldingMode) || dbType.equals("semantic")) {
                if (dbType.equals("semantic")) {
                    SemanticIndexMappingGenerator map_gen = new SemanticIndexMappingGenerator(ds, apic, dag);
                    map_gen.build();
                }
                List<OBDAMappingAxiom> mappings = apic.getMappingController().getMappings(ds.getSourceID());

                // Validate the mappings against the ontology
                OWLOntology owlOntology = manager.getOntology(uri);
                MappingValidator validator = new MappingValidator(owlOntology);
                validator.validate(mappings);

                MappingViewManager viewMan = new MappingViewManager(mappings);
                unfMech = new ComplexMappingUnfolder(mappings, viewMan);
                JDBCUtility util = new JDBCUtility(ds.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
                gen = new ComplexMappingSQLGenerator(viewMan, util);
            } else if ("material".equals(unfoldingMode)) {
                unfMech = new DirectMappingUnfolder();
                AboxFromDBLoader loader = new AboxFromDBLoader();
                gen = new SimpleDirectQueryGenrator(loader.getMapper(ds));
            } else {
                log.error("Invalid parameter for {}", ReformulationPlatformPreferences.ABOX_MODE);
            }

            /***
             * Done, sending a new reasoner with the modules we just configured
             */

            techniqueWrapper = new BolzanoTechniqueWrapper(unfMech, rewriter, gen, eval_engine, apic);
            log.debug("... Quest has been setup and is ready for querying");

            return new OBDAOWLReformulationPlatform(apic, manager, techniqueWrapper);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    public OBDAModel getApiController() {
        return apic;
    }
}
