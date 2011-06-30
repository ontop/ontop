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
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DirectMappingGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.MappingValidator;
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
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.DLRPerfectReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryVocabularyValidator;
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
import java.util.ArrayList;
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
 * The implementation of the factory for creating reformulation's platform
 * reasoner
 */

public class OBDAOWLReformulationPlatformFactoryImpl implements OBDAOWLReformulationPlatformFactory {

	private OBDAModel							apic;
	private ReformulationPlatformPreferences	preferences	= null;
	private String								id;
	private String								name;
	private OWLOntologyManager					owlOntologyManager;

	private final Logger						log			= LoggerFactory.getLogger(OBDAOWLReformulationPlatformFactoryImpl.class);

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
		return new OBDAOWLReformulationPlatform(manager);
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
