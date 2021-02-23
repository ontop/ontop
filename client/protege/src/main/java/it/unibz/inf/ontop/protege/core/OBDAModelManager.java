package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.protege.connection.DataSource;
import it.unibz.inf.ontop.protege.mapping.DuplicateTriplesMapException;
import it.unibz.inf.ontop.protege.mapping.TriplesMapCollection;
import it.unibz.inf.ontop.protege.query.QueryManager;
import it.unibz.inf.ontop.protege.query.QueryManagerEventListener;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.apache.commons.rdf.api.RDF;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.util.*;

public class OBDAModelManager implements Disposable {

	private static final String OBDA_EXT = ".obda"; // The default OBDA file extension.
	private static final String QUERY_EXT = ".q"; // The default query file extension.
	public static final String PROPERTY_EXT = ".properties"; // The default property file extension.

	private final OWLEditorKit owlEditorKit;

	// mutable
	private final DataSource datasource = new DataSource();
	// mutable
	private final TriplesMapCollection triplesMapCollection;
	// Mutable: the ontology inside is replaced
	private final OntologySignature currentMutableVocabulary = new OntologySignature();
	// mutable
	private final QueryManager queryManager = new QueryManager();


	private final List<OBDAModelManagerListener> obdaManagerListeners = new ArrayList<>();

	private final JDBCConnectionManager connectionManager = JDBCConnectionManager.getJDBCConnectionManager();

	private final OntopConfigurationManager configurationManager;

	private final RDF rdfFactory;
	private final TypeFactory typeFactory;

	private static final Logger LOGGER = LoggerFactory.getLogger(OBDAModelManager.class);

	/***
	 * This is the instance responsible for listening for Protege ontology
	 * events (loading/saving/changing ontology)
	 */
	private final OWLModelManagerListener modelManagerListener = new OBDAPluginOWLModelManagerListener();

	/***
	 * This flag is used to avoid triggering a "Ontology Changed" event when new
	 * mappings/sources/queries are inserted into the model not by the user, but
	 * by a ontology load call.
	 */
	private boolean loadingData;

	@Nullable
	private OWLOntologyID lastKnownOntologyId;

	public OBDAModelManager(OWLEditorKit editorKit) {
		this.owlEditorKit = editorKit;
		/*
		 * TODO: avoid using Default injector
		 */
		Injector defaultInjector = OntopMappingSQLAllConfiguration.defaultBuilder()
				.jdbcDriver("")
				.jdbcUrl("")
				.jdbcUser("")
				.jdbcPassword("")
				.build().getInjector();

		SQLPPMappingFactory ppMappingFactory = defaultInjector.getInstance(SQLPPMappingFactory.class);
		TermFactory termFactory = defaultInjector.getInstance(TermFactory.class);
		typeFactory = defaultInjector.getInstance(TypeFactory.class);
		rdfFactory = defaultInjector.getInstance(RDF.class);
		TargetAtomFactory targetAtomFactory = defaultInjector.getInstance(TargetAtomFactory.class);
		SubstitutionFactory substitutionFactory = defaultInjector.getInstance(SubstitutionFactory.class);
		TargetQueryParserFactory targetQueryParserFactory = defaultInjector.getInstance(TargetQueryParserFactory.class);
		SQLPPSourceQueryFactory sourceQueryFactory = defaultInjector.getInstance(SQLPPSourceQueryFactory.class);

		getModelManager().addListener(modelManagerListener);
		getOntologyManager().addOntologyChangeListener(new OntologyRefactoringListener());

		triplesMapCollection = new TriplesMapCollection(getActiveOntology(), ppMappingFactory, termFactory,
				targetAtomFactory, substitutionFactory, targetQueryParserFactory, sourceQueryFactory);

		triplesMapCollection.addMappingsListener(this::triggerOntologyChanged);

		queryManager.addListener(new QueryManagerEventListener() {
			@Override
			public void inserted(QueryManager.Item group, int indexInParent) {
				triggerOntologyChanged();
			}
			@Override
			public void removed(QueryManager.Item group, int indexInParent) {
				triggerOntologyChanged();
			}
			@Override
			public void renamed(QueryManager.Item group, int indexInParent) {
				triggerOntologyChanged();
			}
			@Override
			public void changed(QueryManager.Item group, int indexInParent) {
				triggerOntologyChanged();
			}
		});

		datasource.addListener(this::triggerOntologyChanged);

		configurationManager = new OntopConfigurationManager(this, OBDAEditorKitSynchronizerPlugin.getProperties(owlEditorKit));
	}

	/***
	 * Called from ModelManager dispose method since this object is setup as the
	 * APIController.class.getName() property with the put method.
	 */
	@Override
	public void dispose() {
		try {
			getModelManager().removeListener(modelManagerListener);
			connectionManager.dispose();
		}
		catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}
	}


	private OWLModelManager getModelManager() {
		return owlEditorKit.getModelManager();
	}

	private OWLOntology getActiveOntology() {
		return getModelManager().getActiveOntology();
	}

	private OWLOntologyManager getOntologyManager() {
		return getModelManager().getOWLOntologyManager();
	}

	/***
	 * This ontology change listener has some heuristics to determine if the
	 * user is refactoring the ontology. In particular, this listener will try
	 * to determine if some add/remove axioms are in fact a "renaming"
	 * operation. This happens when a list of axioms has a
	 * remove(DeclarationAxiom(x)) immediately followed by an
	 * add(DeclarationAxiom(y)). In this case, y is a new name for x.
	 */
	public class OntologyRefactoringListener implements OWLOntologyChangeListener {

		@Override
		public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes) {
			Map<OWLEntity, OWLEntity> renamings = new HashMap<>();
			Set<OWLEntity> removals = new HashSet<>();

			for (int idx = 0; idx < changes.size() ; idx++) {
				OWLOntologyChange change = changes.get(idx);
				if (change instanceof SetOntologyID) {
					OWLOntologyID newID = ((SetOntologyID) change).getNewOntologyID();
					LOGGER.debug("Ontology ID changed\nOld ID: {}\nNew ID: {}", ((SetOntologyID) change).getOriginalOntologyID(), newID);

					getMutablePrefixManager().updateOntologyID(newID);
				}
				else if (change instanceof RemoveAxiom) {
					OWLAxiom axiom = change.getAxiom();
					if (idx + 1 < changes.size() && changes.get(idx + 1) instanceof AddAxiom) {
						// renaming
						OWLAxiom nextAxiom = changes.get(idx + 1).getAxiom();
						if (axiom instanceof OWLDeclarationAxiom && nextAxiom instanceof OWLDeclarationAxiom) {
							renamings.put(((OWLDeclarationAxiom) axiom).getEntity(),
									((OWLDeclarationAxiom) nextAxiom).getEntity());
						}
					}
					else if (axiom instanceof OWLDeclarationAxiom) {
						removals.add(((OWLDeclarationAxiom) axiom).getEntity());
					}
				}
			}

			for (Map.Entry<OWLEntity, OWLEntity> e : renamings.entrySet())
				triplesMapCollection.renamePredicate(getIRI(e.getKey()), getIRI(e.getValue()));

			for (OWLEntity entity : removals)
				triplesMapCollection.removePredicate(getIRI(entity));
		}

		private org.apache.commons.rdf.api.IRI getIRI(OWLEntity entity) {
			return rdfFactory.createIRI(entity.getIRI().toString());
		}
	}



	public void addListener(OBDAModelManagerListener listener) {
		obdaManagerListeners.add(listener);
	}

	public void removeListener(OBDAModelManagerListener listener) {
		obdaManagerListeners.remove(listener);
	}



	public RDF getRdfFactory() {
		return rdfFactory;
	}

	public DataSource getDataSource() {
		return datasource;
	}

	public TriplesMapCollection getTriplesMapCollection() {
		return triplesMapCollection;
	}

	public QueryManager getQueryManager() {
		return queryManager;
	}

	public OntologySignature getCurrentVocabulary() { return currentMutableVocabulary; }

	public MutablePrefixManager getMutablePrefixManager() { return triplesMapCollection.getMutablePrefixManager(); }


	private class OBDAPluginOWLModelManagerListener implements OWLModelManagerListener {
		// TODO: clean up code - this one is called from the event dispatch thread
		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {
			LOGGER.debug(event.getType().name());
			switch (event.getType()) {
				case ABOUT_TO_CLASSIFY:
					loadingData = true;
					break;

				case ONTOLOGY_CLASSIFIED:
					loadingData = false;
					break;

				case ACTIVE_ONTOLOGY_CHANGED:
					handleNewActiveOntology();
					break;

				case ONTOLOGY_LOADED:
				case ONTOLOGY_RELOADED:
					handleOntologyLoadedAndReLoaded();
					break;

				case ONTOLOGY_SAVED:
					handleOntologySaved();
					break;

				case ENTITY_RENDERER_CHANGED:
				case REASONER_CHANGED:
				case ONTOLOGY_VISIBILITY_CHANGED:
				case ONTOLOGY_CREATED:
				case ENTITY_RENDERING_CHANGED:
					break;
			}
		}
	}

	/**
	 * When the active ontology is new (first one or differs from the last one)
	 */
	private void handleNewActiveOntology() {
		OWLOntology ontology = getActiveOntology();
		OWLOntologyID id = ontology.getOntologyID();
		if (id.equals(lastKnownOntologyId))
			return;

		lastKnownOntologyId = id;

		currentMutableVocabulary.reset(ontology);

		triplesMapCollection.reset(ontology);

		configurationManager.reset(OBDAEditorKitSynchronizerPlugin.getProperties(owlEditorKit));

		datasource.reset();

		ProtegeOWLReasonerInfo factory = getModelManager().getOWLReasonerManager().getCurrentReasonerFactory();
		if (factory instanceof OntopReasonerInfo) {
			OntopReasonerInfo questfactory = (OntopReasonerInfo) factory;
			questfactory.setConfigurationGenerator(configurationManager);
		}
		fireActiveOBDAModelChange();
	}

	private void handleOntologyLoadedAndReLoaded() {
		loadingData = true; // flag on
		try {
			String owlName = getActiveOntologyOwlFilename();
			if (owlName == null)
				return;

			File obdaFile = new File(URI.create(owlName + OBDA_EXT));
			if (obdaFile.exists()) {
				configurationManager.load(owlName);
				datasource.load(new File(URI.create(owlName + PROPERTY_EXT)));
				triplesMapCollection.load(obdaFile, this); // can update datasource!
				queryManager.load(new File(URI.create(owlName + QUERY_EXT)));
			}
			else {
				LOGGER.warn("No OBDA model was loaded because no .obda file exists in the same location as the .owl file");
			}
		}
		catch (Exception e) {
			InvalidOntopConfigurationException ex = new InvalidOntopConfigurationException("An exception has occurred when loading input file.\nMessage: " + e.getMessage());
			DialogUtils.showQuickErrorDialog(null, ex, "Open file error");
			LOGGER.error(e.getMessage());
		}
		finally {
			loadingData = false; // flag off
			fireActiveOBDAModelChange();
		}
	}

	private void handleOntologySaved() {
		String owlName = getActiveOntologyOwlFilename();
		if (owlName == null)
			return;

		try {
			triplesMapCollection.store(new File(URI.create(owlName + OBDA_EXT)));
			queryManager.store(new File(URI.create(owlName + QUERY_EXT)));
			datasource.store(new File(URI.create(owlName + PROPERTY_EXT)));
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
			Exception newException = new Exception(
					"Error saving the OBDA file. Closing Protege now can result in losing changes " +
							"in your data sources or mappings. Please resolve the issue that prevents " +
							"saving in the current location, or do \"Save as..\" to save in an alternative location. \n\n" +
							"The error message was: \n"  + e.getMessage());
			DialogUtils.showQuickErrorDialog(null, newException, "Error saving OBDA file");
			triggerOntologyChanged();
		}
	}

	private String getActiveOntologyOwlFilename() {
		IRI documentIRI = getOntologyManager().getOntologyDocumentIRI(getActiveOntology());

		if (!UIUtil.isLocalFile(documentIRI.toURI()))
			return null;

		String owlDocumentIriString = documentIRI.toString();
		int i = owlDocumentIriString.lastIndexOf(".");
		return owlDocumentIriString.substring(0, i);
	}

	private void fireActiveOBDAModelChange() {
		for (OBDAModelManagerListener listener : obdaManagerListeners) {
			try {
				listener.activeOntologyChanged();
			}
			catch (Exception e) {
				LOGGER.debug("Badly behaved listener: {}", listener.getClass().toString());
				LOGGER.debug(e.getMessage(), e);
			}
		}
	}

	private void triggerOntologyChanged() {
		if (loadingData)
			return;

		OWLOntology ontology = getActiveOntology();
		if (ontology == null)
			return;

		getModelManager().setDirty(ontology);
	}

	public Set<OWLDeclarationAxiom> insertTriplesMaps(ImmutableList<SQLPPTriplesMap> triplesMaps, boolean bootstraped) throws DuplicateTriplesMapException {
		getTriplesMapCollection().addAll(triplesMaps);

		return MappingOntologyUtils.extractAndInsertDeclarationAxioms(
				getActiveOntology(),
				triplesMaps,
				typeFactory,
				bootstraped);
	}

	public void addAxiomsToOntology(Set<? extends OWLAxiom> axioms) {
		getOntologyManager().addAxioms(getActiveOntology(), axioms);
	}


	public OntopSQLOWLAPIConfiguration getConfigurationForOntology() {
		return configurationManager.buildOntopSQLOWLAPIConfiguration(getActiveOntology());
	}

	public SQLPPMapping parseR2RML(File file) throws MappingException {
		OntopMappingSQLAllConfiguration configuration = configurationManager.buildR2RMLConfiguration(datasource, file);
		return configuration.loadProvidedPPMapping();
	}

	public SQLPPMapping parseOBDA(String mapping) throws MappingIOException, InvalidMappingException {
		Reader mappingReader = new StringReader(mapping);
		SQLMappingParser mappingParser = configurationManager.getSQLMappingParser(datasource, mappingReader);
		return mappingParser.parse(mappingReader);
	}
}
