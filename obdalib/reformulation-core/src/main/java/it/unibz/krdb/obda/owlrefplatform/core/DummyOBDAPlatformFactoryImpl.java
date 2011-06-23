package it.unibz.krdb.obda.owlrefplatform.core;


import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexMappingGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticReduction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterOntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeRedReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.ComplexMappingSQLGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.ComplexMappingUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.MappingViewManager;
import it.unibz.krdb.sql.JDBCConnectionManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DummyOBDAPlatformFactoryImpl implements OBDAOWLReformulationPlatformFactory {

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


    @Override
    public String getReasonerName() {
        return this.name;
    }

    @Override
    public OWLReasoner createReasoner(OWLOntologyManager manager) {

        QueryRewriter rewriter;
        MappingViewManager viewMan;
        UnfoldingMechanism unfMech;
        JDBCUtility util;
        SourceQueryGenerator gen;
        BolzanoTechniqueWrapper techniqueWrapper;
        try {
            Set<OWLOntology> ontologies = manager.getOntologies();
            URI uri = uri = ontologies.iterator().next().getURI();
            OWLAPITranslator translator = new OWLAPITranslator();
            DLLiterOntology ontology = new DLLiterOntologyImpl(uri);

            for (OWLOntology onto : ontologies) {
                DLLiterOntology aux = translator.translate(onto);
                ontology.addAssertions(aux.getAssertions());
                ontology.addConcepts(new ArrayList<ConceptDescription>(aux.getConcepts()));
                ontology.addRoles(new ArrayList<RoleDescription>(aux.getRoles()));
            }

            DAG isa = DAGConstructor.getISADAG(ontology);
            DAG pureIsa = DAGConstructor.filterPureISA(isa);
            pureIsa.index();
            if (GraphGenerator.debugInfoDump) {
                GraphGenerator.dumpISA(isa);
            }

            SemanticReduction reducer = new SemanticReduction(isa, DAGConstructor.getSigma(ontology));
            List<Assertion> reducedOnto = reducer.reduce();
            if (GraphGenerator.debugInfoDump) {
                GraphGenerator.dumpReducedOnto(reducedOnto);
            }

            // Mappings
            DataSource ds = apic.getDatasourcesController().getAllSources().get(0);
            Connection connection = JDBCConnectionManager.getJDBCConnectionManager().getConnection(ds);

            EvaluationEngine eval_engine = eval_engine = new JDBCEngine(connection);
            List<OBDAMappingAxiom> mappings = new ArrayList<OBDAMappingAxiom>();
            for (OBDAMappingAxiom map : SemanticIndexMappingGenerator.build(isa, pureIsa)) {
                mappings.add(map);
                apic.getMappingController().insertMapping(ds.getSourceID(), map);
            }


            // Rewriter
            rewriter = new TreeRedReformulator(reducedOnto);

            // Source query generator and unfolder
            viewMan = new MappingViewManager(mappings);
            unfMech = new ComplexMappingUnfolder(mappings, viewMan);
            util = new JDBCUtility(ds.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
            gen = new ComplexMappingSQLGenerator(viewMan, util);

            techniqueWrapper = new BolzanoTechniqueWrapper(unfMech, rewriter, gen, eval_engine, apic);
            return new OBDAOWLReformulationPlatform(apic, manager, techniqueWrapper);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
