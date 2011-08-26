package it.unibz.krbd.obda.TWrewriting;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlapi.OBDAOWLReasoner;
import it.unibz.krdb.obda.owlapi.OBDAOWLReasonerFactory;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.GraphGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.core.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.core.QuestTechniqueWrapper;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticReduction;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeWitnessReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2Translator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.inference.OWLReasonerFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TWOBDAPlatformFactoryImpl implements OBDAOWLReasonerFactory, OWLReasonerFactory {

	private OBDAModel							apic;
	private ReformulationPlatformPreferences	preferences	= null;
	private String								id;
	private String								name;
	private OWLOntologyManager					owlOntologyManager;

	private final Logger						log			= LoggerFactory.getLogger(QuestOWLFactory.class);

	// @Override
	// public void setOBDAController(OBDAModel controller) {
	// this.apic = controller;
	//
	// }

	@Override
	public void setPreferenceHolder(ReformulationPlatformPreferences preference) {
		this.preferences = preference;

	}

	@Override
	public OBDAOWLReasoner createReasoner(OWLOntologyManager manager) {

		TreeWitnessReformulator rewriter;
		// MappingViewManager viewMan;
		UnfoldingMechanism unfMech;
		// JDBCUtility util;
		SourceQueryGenerator gen;
		QuestTechniqueWrapper techniqueWrapper;
		try {
			Set<OWLOntology> ontologies = manager.getOntologies();
			URI uri = ontologies.iterator().next().getURI();
			OWLAPI2Translator translator = new OWLAPI2Translator();
			DLLiterOntology ontology = new OntologyImpl(uri);

			for (OWLOntology onto : ontologies) {
				DLLiterOntology aux = translator.translate(onto);
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
			/*
			 * DataSource ds =
			 * apic.getDatasourcesController().getAllSources().get(0);
			 * Connection connection =
			 * JDBCConnectionManager.getJDBCConnectionManager
			 * ().getConnection(ds);
			 * 
			 * EvaluationEngine eval_engine = new JDBCEngine(connection);
			 * List<SemanticIndexMappingGenerator.MappingKey> simple_mappings =
			 * SemanticIndexMappingGenerator.build(isa, pureIsa);
			 * List<OBDAMappingAxiom> mappings = new
			 * ArrayList<OBDAMappingAxiom>(); for (OBDAMappingAxiom map :
			 * SemanticIndexMappingGenerator.compile(simple_mappings)) {
			 * mappings.add(map);
			 * apic.getMappingController().insertMapping(ds.getSourceID(), map);
			 * }
			 */

			// Rewriter
			rewriter = new TreeWitnessReformulator();
			rewriter.setTBox(ontology);

			EvaluationEngine eval_engine = null;
			// Source query generator and unfolder
			// viewMan = null; //new MappingViewManager(mappings);
			unfMech = null; // new ComplexMappingUnfolder(mappings, viewMan);
			// util = null; //new
			// JDBCUtility(ds.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			gen = null; // new ComplexMappingSQLGenerator(viewMan, util);

			techniqueWrapper = new QuestTechniqueWrapper(unfMech, rewriter, gen, null, eval_engine, apic);

			QuestOWL reasoner = new QuestOWL(manager);

			reasoner.setTechniqueWrapper(techniqueWrapper);
			reasoner.loadOBDAModel(apic);

			return reasoner;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getReasonerName() {
		// TODO Auto-generated method stub
		return this.name;
	}

}
