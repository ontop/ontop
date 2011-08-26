package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlapi.OBDAOWLReasoner;
import it.unibz.krdb.obda.owlapi.OBDAOWLReasonerFactory;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticReduction;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeRedReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.ComplexMappingSQLGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2Translator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.ComplexMappingUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.MappingViewManager;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.net.URI;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyOBDAPlatformFactoryImpl implements OBDAOWLReasonerFactory {

	private OBDAModel							apic;
	private ReformulationPlatformPreferences	preferences	= null;
	private String								id;
	private String								name;
	private OWLOntologyManager					owlOntologyManager;

	private final Logger						log			= LoggerFactory.getLogger(QuestOWLFactory.class);

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

//	@Override
//	public void setOBDAController(OBDAModel apic) {
//		this.apic = apic;
//	}

	@Override
	public void setPreferenceHolder(ReformulationPlatformPreferences preference) {
		this.preferences = preference;
	}

	@Override
	public String getReasonerName() {
		return this.name;
	}

	@Override
	public OBDAOWLReasoner createReasoner(OWLOntologyManager manager) {

		QueryRewriter rewriter;
		MappingViewManager viewMan;
		UnfoldingMechanism unfMech;
		JDBCUtility util;
		SourceQueryGenerator gen;
		QuestTechniqueWrapper techniqueWrapper;
		try {
			Set<OWLOntology> ontologies = manager.getOntologies();
			URI uri = uri = ontologies.iterator().next().getURI();
			OWLAPI2Translator translator = new OWLAPI2Translator();
			Ontology ontology = new OntologyImpl(uri);

			for (OWLOntology onto : ontologies) {
				Ontology aux = translator.translate(onto);
				ontology.addAssertions(aux.getAssertions());
				ontology.addConcepts(new ArrayList<ClassDescription>(aux.getConcepts()));
				ontology.addRoles(new ArrayList<Property>(aux.getRoles()));
			}

			DAG isa = DAGConstructor.getISADAG(ontology);
			DAG pureIsa = DAGConstructor.filterPureISA(isa);
			pureIsa.index();
			if (GraphGenerator.debugInfoDump) {
				GraphGenerator.dumpISA(isa, "general");
				GraphGenerator.dumpISA(pureIsa, "simple");
			}

			SemanticReduction reducer = new SemanticReduction(ontology, DAGConstructor.getSigmaOntology(ontology));
			List<Axiom> reducedOnto = reducer.reduce();
			if (GraphGenerator.debugInfoDump) {
				GraphGenerator.dumpReducedOnto(reducedOnto);
			}

			// Mappings
			OBDADataSource ds = apic.getSources().get(0);
			Connection connection = JDBCConnectionManager.getJDBCConnectionManager().getConnection(ds);

			EvaluationEngine eval_engine = eval_engine = new JDBCEngine(connection);
			RDBMSSIRepositoryManager man = new RDBMSSIRepositoryManager(ds);
			man.setTBox(ontology);
			apic.addMappings(ds.getSourceID(), man.getMappings());

			// Rewriter
			Ontology dlliteontology = new OntologyImpl(URI.create("http://it.unibz.krdb/obda/auxontology"));
			dlliteontology.addAssertions(reducedOnto);
			rewriter = new TreeRedReformulator();
			rewriter.setTBox(dlliteontology);

			rewriter.setCBox(DAGConstructor.getSigmaOntology(ontology));

			// Source query generator and unfolder
			viewMan = new MappingViewManager(man.getMappings());
			unfMech = new ComplexMappingUnfolder(man.getMappings(), viewMan);
			util = new JDBCUtility(ds.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			gen = new ComplexMappingSQLGenerator(viewMan, util);

			techniqueWrapper = new QuestTechniqueWrapper(unfMech, rewriter, gen, null, eval_engine, apic);
			return new QuestOWL(manager);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
