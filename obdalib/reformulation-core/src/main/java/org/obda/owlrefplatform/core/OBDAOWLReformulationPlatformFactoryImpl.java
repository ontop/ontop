package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.api.datasource.JDBCConnectionManager;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.owlapi.ReformulationPlatformPreferences;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;
import org.obda.owlrefplatform.core.abox.*;
import org.obda.owlrefplatform.core.ontology.Assertion;
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
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * The implementation of the factory for creating reformulation's platform reasoner
 *
 * @author Manfred Gerstgrasser
 */

public class OBDAOWLReformulationPlatformFactoryImpl implements OBDAOWLReformulationPlatformFactory {

    private APIController apic;
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
    public void setOBDAController(APIController apic) {
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
            if (ontologies.size() > 0) {
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

            if (useInMemoryDB) {
                log.debug("Using in an memory database");
                String driver = "org.h2.Driver";
                String url = "jdbc:h2:mem:aboxdump";
                String username = "sa";
                String password = "";
                Connection connection;

                DataSource source = new DataSource(URI.create("http://www.obda.org/ABOXDUMP"));
                source.setParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER, driver);
                source.setParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD, password);
                source.setParameter(RDBMSsourceParameterConstants.DATABASE_URL, url);
                source.setParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME, username);
                source.setParameter(RDBMSsourceParameterConstants.IS_IN_MEMORY, "true");
                source.setParameter(RDBMSsourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

                apic.getDatasourcesController().addDataSource(source);
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
                MappingViewManager viewMan = new MappingViewManager(mappings);
                unfMech = new ComplexMappingUnfolder(mappings, viewMan);
                JDBCUtility util = new JDBCUtility(ds.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER));
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
    public APIController getApiController() {
        return apic;
    }
}
