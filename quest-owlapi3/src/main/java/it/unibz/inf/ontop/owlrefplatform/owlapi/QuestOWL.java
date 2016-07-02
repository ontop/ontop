package it.unibz.inf.ontop.owlrefplatform.owlapi;

/*
 * #%L
 * ontop-quest-owlapi
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Guice;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDACoreModule;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.ResultSet;
import it.unibz.inf.ontop.model.TupleResultSet;
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.owlapi.OWLAPIABoxIterator;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlrefplatform.core.*;
import it.unibz.inf.ontop.owlrefplatform.core.abox.QuestMaterializer;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestComponentFactory;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestComponentModule;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;
import it.unibz.inf.ontop.utils.VersionInfo;
import org.openrdf.model.Model;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.impl.OWLReasonerBase;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * The OBDAOWLReformulationPlatform implements the OWL reasoner interface and is
 * the implementation of the reasoning method in the reformulation project.
 */
public class QuestOWL extends OWLReasonerBase implements AutoCloseable {

    StructuralReasoner structuralReasoner;

    private final Version version;

	private boolean interrupted = false;

	private final ReasonerProgressMonitor pm;

	private boolean prepared = false;

	private boolean questready = false;
	
	private Object inconsistent = null;

	// / holds the error that quest had when initializing
	private String errorMessage = "";

	private Exception questException = null;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// From Quest
	//
	// //////////////////////////////////////////////////////////////////////////////////////

	/* The merge and tranlsation of all loaded ontologies */
	private Ontology translatedOntologyMerge;

	private final OBDAModel obdaModel;

	private final QuestPreferences preferences;

	private IQuest questInstance = null;

	private static Logger log = LoggerFactory.getLogger(QuestOWL.class);

	private IQuestConnection conn = null;

	private QuestOWLConnection owlconn = null;

	private final OWLOntologyManager man;
	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	//  User Constraints are primary and foreign keys not in the database 
	//  
	//
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//  Davide>
	//  T-Mappings Configuration
	//  
	//
	// //////////////////////////////////////////////////////////////////////////////////////

	/* Used to enable querying annotation Properties coming from the ontology. */

	/* Used to enable use of same as in mappings. */

	private final QuestComponentFactory componentFactory;
	
	/* Used to signal whether to apply the user constraints above */
	//private boolean applyExcludeFromTMappings = false;


    public QuestOWL(OWLOntology rootOntology, QuestOWLConfiguration configuration)
			throws IllegalConfigurationException {
        super(rootOntology, configuration, BufferingMode.BUFFERING);
        this.structuralReasoner = new StructuralReasoner(rootOntology, configuration, BufferingMode.BUFFERING);
		preferences = configuration.getPreferences();

		Injector injector = Guice.createInjector(new OBDACoreModule(preferences), new QuestComponentModule(preferences));
		this.componentFactory = injector.getInstance(QuestComponentFactory.class);
		NativeQueryLanguageComponentFactory nativeQLFactory = injector.getInstance(NativeQueryLanguageComponentFactory.class);

		try {
			if (preferences.isInVirtualMode()) {
				/**
				 * OBDA model extraction (virtual mode)
				 */
				Optional<File> optionalFile = preferences.getMappingFile();
				if (optionalFile.isPresent()) {
					File mappingFile = optionalFile.get();
					obdaModel = nativeQLFactory.create(mappingFile).getOBDAModel();
				} else {
					Optional<Reader> optionalReader = preferences.getMappingReader();
					if (optionalReader.isPresent()) {
						obdaModel = nativeQLFactory.create(optionalReader.get()).getOBDAModel();
					} else {
						Optional<Model> optionalModel = preferences.getMappingModel();
						if (optionalModel.isPresent()) {
							obdaModel = nativeQLFactory.create(optionalModel.get()).getOBDAModel();
						} else {
							Optional<OBDAModel> optionalPredefinedOBDAModel = preferences.getPredefinedOBDAModel();
							if (optionalPredefinedOBDAModel.isPresent()) {
								obdaModel = optionalPredefinedOBDAModel.get();
							} else {
								throw new IllegalConfigurationException("Mappings are expected in the virtual mode", configuration);
							}
						}
					}
				}
			}
			/**
			 * Classic mode
			 */
			else {
				obdaModel = null;
			}

			/**
			 * Mapping parsing exceptions are re-thrown as configuration exceptions.
			 */
		} catch (InvalidDataSourceException | IOException | InvalidMappingException e) {
			throw new IllegalConfigurationException(e.getMessage(), configuration);
		}

		pm = configuration.getProgressMonitor();

		man = rootOntology.getOWLOntologyManager();

		version = extractVersion();

		prepareReasoner();
    }


	/**
	 * extract version from {@link it.unibz.inf.ontop.utils.VersionInfo}, which is from the file {@code version.properties}
	 */
	private static Version extractVersion() {
		VersionInfo versionInfo = VersionInfo.getVersionInfo();
		String versionString = versionInfo.getVersion();
		String[] splits = versionString.split("\\.");
		int major = 0;
		int minor = 0;
		int patch = 0;
		int build = 0;
		try {
			major = Integer.parseInt(splits[0]);
			minor = Integer.parseInt(splits[1]);
			patch = Integer.parseInt(splits[2]);
			build = Integer.parseInt(splits[3]);
		} catch (Exception ignored) {

		}
		return new Version(major, minor, patch, build);
	}

	@Override
    public void flush() {
		prepared = false;
		
		super.flush();
		
		prepareReasoner();
		
	}
	
	@Deprecated // used in one test only
	public IQuest getQuestInstance() {
		return questInstance;
	}

	public QuestOWLStatement getStatement() throws OWLException {
		if (!questready) {
			OWLReasonerRuntimeException owlReasonerRuntimeException = new ReasonerInternalException(
					"Quest was not initialized properly. This is generally indicates, connection problems or error during ontology or mapping pre-processing. \n\nOriginal error message:\n" + questException.getMessage()) ;
				owlReasonerRuntimeException.setStackTrace(questException.getStackTrace());
			throw owlReasonerRuntimeException;
		}
		return owlconn.createStatement();
	}

	private void prepareQuestInstance() throws Exception {

		try {
			if (questInstance != null)
				questInstance.dispose();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}

		log.debug("Initializing a new Quest instance...");

		final boolean bObtainFromOntology = preferences.getBoolean(QuestPreferences.OBTAIN_FROM_ONTOLOGY);
		final boolean bObtainFromMappings = preferences.getBoolean(QuestPreferences.OBTAIN_FROM_MAPPINGS);
		final String unfoldingMode = preferences.getProperty(QuestPreferences.ABOX_MODE);

		// pm.reasonerTaskStarted("Classifying...");
		// pm.reasonerTaskBusy();

		questInstance = componentFactory.create(translatedOntologyMerge, obdaModel, null, preferences);
		
		Set<OWLOntology> importsClosure = man.getImportsClosure(getRootOntology());
		

		try {
			// pm.reasonerTaskProgressChanged(1, 4);

			// Setup repository
			questInstance.setupRepository();
			// pm.reasonerTaskProgressChanged(2, 4);

			// Retrives the connection from Quest
			//conn = questInstance.getConnection();
			conn = questInstance.getNonPoolConnection();
			owlconn = new QuestOWLConnection(conn);
			// pm.reasonerTaskProgressChanged(3, 4);

			// Preparing the data source
			if (unfoldingMode.equals(QuestConstants.CLASSIC)) {
				IQuestStatement st = conn.createStatement();
				if (bObtainFromOntology) {
					// Retrieves the ABox from the ontology file.
					log.debug("Loading data from Ontology into the database");
					OWLAPIABoxIterator aBoxIter = new OWLAPIABoxIterator(importsClosure, questInstance.getVocabulary());
					int count = st.insertData(aBoxIter, 5000, 500);
					log.debug("Inserted {} triples from the ontology.", count);
				}
				if (bObtainFromMappings) { // TODO: GUOHUI 2016-01-16: This mode will be removed
					// Retrieves the ABox from the target database via mapping.
					log.debug("Loading data from Mappings into the database");

					OBDAModel obdaModelForMaterialization = questInstance.getOBDAModel();
					obdaModelForMaterialization.getOntologyVocabulary().merge(translatedOntologyMerge.getVocabulary());
					
					QuestMaterializer materializer = new QuestMaterializer(obdaModelForMaterialization, false);
					Iterator<Assertion> assertionIter = materializer.getAssertionIterator();
					int count = st.insertData(assertionIter, 5000, 500);
					materializer.disconnect();
					log.debug("Inserted {} triples from the mappings.", count);
				}
//				st.createIndexes();
				st.close();
				if (!conn.getAutoCommit())
				conn.commit();
				
				//questInstance.updateSemanticIndexMappings();
			} else {
				// VIRTUAL MODE - NO-OP
			}
			questready = true;
			log.debug("Quest has completed the setup and it is ready for query answering!");
		} catch (Exception e) {
			throw e;
		} finally {
			// pm.reasonerTaskProgressChanged(4, 4);
			// pm.reasonerTaskStopped();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		try {
			conn.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
		
		try {
			questInstance.dispose();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	/***
	 * This method loads the given ontologies in the system. This will merge
	 * these new ontologies with the existing ones in a set. Then it will
	 * translate the assertions in all the ontologies into a single one, in our
	 * internal representation.
	 * 
	 * The translation is done using our OWLAPITranslator that gets the TBox
	 * part of the ontologies and filters all the DL-Lite axioms (RDFS/OWL2QL
	 * and DL-Lite).
	 * 
	 * The original ontologies and the merged/translated ontology are kept and
	 * are used later when classify() is called.
	 * 
	 */
	public static Ontology loadOntologies(OWLOntology ontology) throws Exception {
		/*
		 * We will keep track of the loaded ontologies and translate the TBox
		 * part of them into our internal representation.
		 */
		log.debug("Load ontologies called. Translating ontologies.");

        Ontology mergeOntology = OWLAPITranslatorUtility.translateImportsClosure(ontology);
        return mergeOntology;
//		log.debug("Ontology loaded: {}", mergeOntology);
	}



	public QuestOWLConnection getConnection() throws OBDAException {
		return owlconn;
	}

	/**
	 * Replaces the owl connection with a new one
	 * Called when the user cancels a query. Easier to get a new connection, than waiting for the cancel
	 * @return The old connection: The caller must close this connection
	 * @throws OBDAException
	 */
	public QuestOWLConnection replaceConnection() throws OBDAException {
		QuestOWLConnection oldconn = this.owlconn;
		conn = questInstance.getNonPoolConnection();
		owlconn = new QuestOWLConnection(conn);
		return oldconn;
	}
	
	@Nonnull
    @Override
	public String getReasonerName() {
		return "Quest";
	}

	@Nonnull
    @Override
	public FreshEntityPolicy getFreshEntityPolicy() {
		return structuralReasoner.getFreshEntityPolicy();
	}

	/**
	 * Gets the IndividualNodeSetPolicy in use by this reasoner. The policy is
	 * set at reasoner creation time.
	 * 
	 * @return The policy.
	 */
	@Nonnull
    @Override
	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        return structuralReasoner.getIndividualNodeSetPolicy();
	}

	/**
	 * Gets the version of this reasoner.
	 * 
	 * @return The version of this reasoner. Not <code>null</code>.
	 */
	@Override
    @Nonnull
    public Version getReasonerVersion() {
		return version;
	}

	@Override
	protected void handleChanges(@Nonnull Set<OWLAxiom> addAxioms, @Nonnull Set<OWLAxiom> removeAxioms) {
		prepared = false;
		prepareReasoner();
	}

	@Override
    public void interrupt() {
		interrupted = true;
	}

	private void ensurePrepared() {
		if (!prepared) {
			prepareReasoner();
		}
	}

	public void prepareReasoner() throws ReasonerInterruptedException, TimeOutException {
		pm.reasonerTaskStarted("Classifying...");
		pm.reasonerTaskBusy();

        structuralReasoner.prepareReasoner();

        try {
			/*
			 * Compute the an ontology with the merge of all the vocabulary and
			 * axioms of the closure of the root ontology
			 */
            this.translatedOntologyMerge = loadOntologies(getRootOntology());

			questready = false;
			questException = null;
			errorMessage = "";
			try {
				prepareQuestInstance();
				questready = true;
				questException = null;
				errorMessage = "";
			} catch (Exception e) {
				questready = false;
				questException = e;
				errorMessage = e.getMessage();
				log.error("Could not initialize the Quest query answering engine. Answering queries will not be available.");
				log.error(e.getMessage(), e);
				throw e;
			}
		} catch (Exception e) {
			throw new ReasonerInternalException(e);
		} finally {
			prepared = true;
			pm.reasonerTaskStopped();
		}

	}

    @Override
	public void precomputeInferences(@Nonnull InferenceType... inferenceTypes) throws ReasonerInterruptedException, TimeOutException,
			InconsistentOntologyException {
		// System.out.println("precomputeInferences");
		ensurePrepared();
		// prepareReasoner();
	}

	@Override
    public boolean isPrecomputed(@Nonnull InferenceType inferenceType) {
		// return true;
		return prepared;
	}

	@Override
    @Nonnull
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        return structuralReasoner.getPrecomputableInferenceTypes();
	}

	@Override
    public boolean isConsistent() {
		return true;
	}
	
	//info to return which axiom was inconsistent during the check
	public Object getInconsistentAxiom() {
		return inconsistent;
	}
	
	public boolean isQuestConsistent() throws ReasonerInterruptedException, TimeOutException {
		return isDisjointAxiomsConsistent() && isFunctionalPropertyAxiomsConsistent();
	}
	
	private boolean isDisjointAxiomsConsistent() throws ReasonerInterruptedException, TimeOutException {

		//deal with disjoint classes
		{
			final String strQueryClass = "ASK {?x a <%s>; a <%s> }";
			
			for (NaryAxiom<ClassExpression> dda : translatedOntologyMerge.getDisjointClassesAxioms()) {
				// TODO: handle complex class expressions and many pairs of disjoint classes
				Collection<ClassExpression> disj = dda.getComponents();
				Iterator<ClassExpression> classIterator = disj.iterator();
				ClassExpression s1 = classIterator.next();
				ClassExpression s2 = classIterator.next();
				String strQuery = String.format(strQueryClass, s1, s2);
				
				boolean isConsistent = executeConsistencyQuery(strQuery);
				if (!isConsistent) {
					inconsistent = dda;
					return false;
				}
			}
		}
		
		//deal with disjoint properties
		{
			final String strQueryProp = "ASK {?x <%s> ?y; <%s> ?y }";

			for(NaryAxiom<ObjectPropertyExpression> dda
						: translatedOntologyMerge.getDisjointObjectPropertiesAxioms()) {		
				// TODO: handle role inverses and multiple arguments			
				Collection<ObjectPropertyExpression> props = dda.getComponents();
				Iterator<ObjectPropertyExpression> iterator = props.iterator();
				ObjectPropertyExpression p1 = iterator.next();
				ObjectPropertyExpression p2 = iterator.next();
				String strQuery = String.format(strQueryProp, p1, p2);
				
				boolean isConsistent = executeConsistencyQuery(strQuery);
				if (!isConsistent) {
					inconsistent = dda;
					return false;
				}
			}
		}
		
		{
			final String strQueryProp = "ASK {?x <%s> ?y; <%s> ?y }";

			for(NaryAxiom<DataPropertyExpression> dda
						: translatedOntologyMerge.getDisjointDataPropertiesAxioms()) {		
				// TODO: handle role inverses and multiple arguments			
				Collection<DataPropertyExpression> props = dda.getComponents();
				Iterator<DataPropertyExpression> iterator = props.iterator();
				DataPropertyExpression p1 = iterator.next();
				DataPropertyExpression p2 = iterator.next();
				String strQuery = String.format(strQueryProp, p1, p2);
				
				boolean isConsistent = executeConsistencyQuery(strQuery);
				if (!isConsistent) {
					inconsistent = dda;
					return false;
				}
			}
		}
		
		return true;
	}
	
	private boolean isFunctionalPropertyAxiomsConsistent() throws ReasonerInterruptedException, TimeOutException {
		
		//deal with functional properties

		final String strQueryFunc = "ASK { ?x <%s> ?y; <%s> ?z. FILTER (?z != ?y) }";
		
		for (ObjectPropertyExpression pfa : translatedOntologyMerge.getFunctionalObjectProperties()) {
			// TODO: handle inverses
			String propFunc = pfa.getName();
			String strQuery = String.format(strQueryFunc, propFunc, propFunc);
			
			boolean isConsistent = executeConsistencyQuery(strQuery);
			if (!isConsistent) {
				inconsistent = pfa;
				return false;
			}
		}
		
		for (DataPropertyExpression pfa : translatedOntologyMerge.getFunctionalDataProperties()) {
			String propFunc = pfa.getName();
			String strQuery = String.format(strQueryFunc, propFunc, propFunc);
			
			boolean isConsistent = executeConsistencyQuery(strQuery);
			if (!isConsistent) {
				inconsistent = pfa;
				return false;
			}
		}
		
		return true;
	}
	
	private boolean executeConsistencyQuery(String strQuery) {
		IQuestStatement query;
		try {
			query = conn.createStatement();
			ResultSet rs = query.execute(strQuery);
			TupleResultSet trs = ((TupleResultSet)rs);
			if (trs!= null && trs.nextRow()){
				String value = trs.getConstant(0).getValue();
				boolean b = Boolean.parseBoolean(value);
				trs.close();
				if (b) 
					return false;
			}
			
		} catch (OBDAException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
    public boolean isSatisfiable(@Nonnull OWLClassExpression classExpression) throws ReasonerInterruptedException, TimeOutException,
			ClassExpressionNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
        return structuralReasoner.isSatisfiable(classExpression);
	}

	@Nonnull
    @Override
    public Node<OWLClass> getUnsatisfiableClasses() throws ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getUnsatisfiableClasses();
	}

	@Override
    public boolean isEntailed(@Nonnull OWLAxiom axiom) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
        return structuralReasoner.isEntailed(axiom);
	}

	@Override
    public boolean isEntailed(@Nonnull Set<? extends OWLAxiom> axioms) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException,
			TimeOutException, AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
        return structuralReasoner.isEntailed(axioms);
	}

	@Override
    public boolean isEntailmentCheckingSupported(@Nonnull AxiomType<?> axiomType) {
        return structuralReasoner.isEntailmentCheckingSupported(axiomType);
	}

	@Nonnull
    @Override
    public Node<OWLClass> getTopClassNode() {
        return structuralReasoner.getTopClassNode();
	}

	@Nonnull
    @Override
    public Node<OWLClass> getBottomClassNode() {
        return structuralReasoner.getBottomClassNode();
	}

	@Nonnull
    @Override
    public NodeSet<OWLClass> getSubClasses(@Nonnull OWLClassExpression ce, boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getSubClasses(ce, direct);
	}

	@Nonnull
    @Override
    public NodeSet<OWLClass> getSuperClasses(@Nonnull OWLClassExpression ce, boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getSuperClasses(ce, direct);
	}

	@Nonnull
    @Override
    public Node<OWLClass> getEquivalentClasses(@Nonnull OWLClassExpression ce) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getEquivalentClasses(ce);
	}

	@Nonnull
    @Override
    public NodeSet<OWLClass> getDisjointClasses(@Nonnull OWLClassExpression ce) {
        return structuralReasoner.getDisjointClasses(ce);
	}

	@Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        return structuralReasoner.getTopObjectPropertyNode();
	}

	@Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        return structuralReasoner.getBottomObjectPropertyNode();
	}

	@Nonnull
    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(@Nonnull OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getSubObjectProperties(pe, direct);
	}

	@Nonnull
    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(@Nonnull OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getSuperObjectProperties(pe, direct);
	}

	@Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(@Nonnull OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getEquivalentObjectProperties(pe);
	}

	@Nonnull
    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(@Nonnull OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getDisjointObjectProperties(pe);
	}

	@Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(@Nonnull OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getInverseObjectProperties(pe);
	}

	@Nonnull
    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(@Nonnull OWLObjectPropertyExpression pe, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getObjectPropertyDomains(pe, direct);
	}

	@Nonnull
    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(@Nonnull OWLObjectPropertyExpression pe, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getObjectPropertyRanges(pe, direct);
	}

	@Nonnull
    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        return structuralReasoner.getTopDataPropertyNode();
	}

	@Nonnull
    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        return structuralReasoner.getBottomDataPropertyNode();
	}

	@Nonnull
    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(@Nonnull OWLDataProperty pe, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getSubDataProperties(pe, direct);
	}

	@Nonnull
    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(@Nonnull OWLDataProperty pe, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getSuperDataProperties(pe, direct);
	}

	@Nonnull
    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(@Nonnull OWLDataProperty pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getEquivalentDataProperties(pe);
	}

	@Nonnull
    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(@Nonnull OWLDataPropertyExpression pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getDisjointDataProperties(pe);
	}

	@Nonnull
    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(@Nonnull OWLDataProperty pe, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getDataPropertyDomains(pe, direct);
	}

	@Nonnull
    @Override
    public NodeSet<OWLClass> getTypes(@Nonnull OWLNamedIndividual ind, boolean direct) throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		return structuralReasoner.getTypes(ind, direct);
	}

	@Nonnull
    @Override
    public NodeSet<OWLNamedIndividual> getInstances(@Nonnull OWLClassExpression ce, boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getInstances(ce, direct);
	}

	@Nonnull
    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(@Nonnull OWLNamedIndividual ind, @Nonnull OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getObjectPropertyValues(ind, pe);
	}


	@Nonnull
    @Override
    public Set<OWLLiteral> getDataPropertyValues(@Nonnull OWLNamedIndividual ind, @Nonnull OWLDataProperty pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
	    return structuralReasoner.getDataPropertyValues(ind, pe);
	}

	@Nonnull
    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(@Nonnull OWLNamedIndividual ind) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getSameIndividuals(ind);
	}

	@Nonnull
    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(@Nonnull OWLNamedIndividual ind) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return structuralReasoner.getDifferentIndividuals(ind);
	}

	/**
	 * Methods to get the empty concepts and roles in the ontology using the given mappings.
	 * It generates SPARQL queries to check for entities.
	 * @return QuestOWLEmptyEntitiesChecker class to get empty concepts and roles
	 * @throws Exception
	 */
	public QuestOWLEmptyEntitiesChecker getEmptyEntitiesChecker() throws Exception {
		QuestOWLEmptyEntitiesChecker empties = new QuestOWLEmptyEntitiesChecker(translatedOntologyMerge, owlconn);
		return empties;
	}

    @Override
	public void close() throws Exception {
		dispose();
	}

}
