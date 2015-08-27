package it.unibz.krdb.obda.owlrefplatform.owlapi3;

/*
 * #%L
 * ontop-quest-owlapi3
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

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.ResultSet;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.owlapi3.OWLAPI3ABoxIterator;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.owlrefplatform.core.*;
import it.unibz.krdb.obda.owlrefplatform.core.abox.EquivalentTriplePredicateIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.QuestMaterializer;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.krdb.obda.utils.VersionInfo;
import it.unibz.krdb.sql.ImplicitDBConstraints;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.*;
import org.semanticweb.owlapi.util.CollectionFactory;
import org.semanticweb.owlapi.util.OWLObjectPropertyManager;
import org.semanticweb.owlapi.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The OBDAOWLReformulationPlatform implements the OWL reasoner interface and is
 * the implementation of the reasoning method in the reformulation project.
 */
public class QuestOWL extends OWLReasonerBase implements AutoCloseable {

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// From Structural Reasoner (to be removed later)
	//
	// //////////////////////////////////////////////////////////////////////////////////////

	private ClassHierarchyInfo classHierarchyInfo = new ClassHierarchyInfo();

	private ObjectPropertyHierarchyInfo objectPropertyHierarchyInfo = new ObjectPropertyHierarchyInfo();

	private DataPropertyHierarchyInfo dataPropertyHierarchyInfo = new DataPropertyHierarchyInfo();

	private Version version;

	private boolean interrupted = false;

	private ReasonerProgressMonitor pm;

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

	private OBDAModel obdaModel = null;

	private QuestPreferences preferences = new QuestPreferences();

	private Quest questInstance = null;

	private static Logger log = LoggerFactory.getLogger(QuestOWL.class);

	private QuestConnection conn = null;

	private QuestOWLConnection owlconn = null;

	private OWLOntologyManager man;
	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	//  User Constraints are primary and foreign keys not in the database 
	//  
	//
	// //////////////////////////////////////////////////////////////////////////////////////
	
	private ImplicitDBConstraints userConstraints = null;
	
	/* Used to signal whether to apply the user constraints above */
	private boolean applyUserConstraints = false;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//  Davide>
	//  T-Mappings Configuration
	//  
	//
	// //////////////////////////////////////////////////////////////////////////////////////

	private TMappingExclusionConfig excludeFromTMappings = TMappingExclusionConfig.empty();
	
	/* Used to signal whether to apply the user constraints above */
	//private boolean applyExcludeFromTMappings = false;
	
	/**
	 * Initialization code which is called from both of the two constructors. 
	 * @param obdaModel 
	 * 
	 */
	private void init(OWLOntology rootOntology, OBDAModel obdaModel, OWLReasonerConfiguration configuration, Properties preferences){
		pm = configuration.getProgressMonitor();
		if (pm == null) {
			pm = new NullReasonerProgressMonitor();
		}

		man = rootOntology.getOWLOntologyManager();

		if (obdaModel != null)
			this.obdaModel = (OBDAModel)obdaModel.clone();
		
		this.preferences.putAll(preferences);

		extractVersion();
		
		prepareReasoner();
	}
	
	/***
	 * Default constructor.
	 */
	public QuestOWL(OWLOntology rootOntology, OBDAModel obdaModel, OWLReasonerConfiguration configuration, BufferingMode bufferingMode,
			Properties preferences) {
		super(rootOntology, configuration, bufferingMode);
		this.init(rootOntology, obdaModel, configuration, preferences);

	}

	/**
	 * This constructor is the same as the default constructor, except that extra constraints (i.e. primary and foreign keys) may be
	 * supplied 
	 * @param userConstraints User-supplied primary and foreign keys
	 */
	public QuestOWL(OWLOntology rootOntology, OBDAModel obdaModel, OWLReasonerConfiguration configuration, BufferingMode bufferingMode,
			Properties preferences, ImplicitDBConstraints userConstraints) {
		super(rootOntology, configuration, bufferingMode);
		
		this.userConstraints = userConstraints;
		assert(userConstraints != null);
		this.applyUserConstraints = true;
		
		this.init(rootOntology, obdaModel, configuration, preferences);
	}
	
	/**
	 * This constructor is the same as the default constructor, 
	 * plus the list of predicates for which TMappings reasoning 
	 * should be disallowed is supplied 
	 * @param excludeFromTMappings from TMappings User-supplied predicates for which TMappings should be forbidden
	 */
	public QuestOWL(OWLOntology rootOntology, OBDAModel obdaModel, OWLReasonerConfiguration configuration, BufferingMode bufferingMode,
			Properties preferences, TMappingExclusionConfig excludeFromTMappings) {
		super(rootOntology, configuration, bufferingMode);
		
		// Davide> T-Mappings handling
		this.excludeFromTMappings = excludeFromTMappings;
		assert(excludeFromTMappings != null);
		
		this.init(rootOntology, obdaModel, configuration, preferences);

	}
	
	/**
	 * This constructor is the same as the default constructor plus the extra constraints, 
	 * but the list of predicates for which TMappings reasoning should be disallowed is 
	 * supplied 
	 * @param excludeFromTMappings User-supplied predicates for which TMappings should be forbidden
	 */
	public QuestOWL(OWLOntology rootOntology, OBDAModel obdaModel, OWLReasonerConfiguration configuration, BufferingMode bufferingMode,
			Properties preferences, ImplicitDBConstraints userConstraints, 
			TMappingExclusionConfig excludeFromTMappings) {
		super(rootOntology, configuration, bufferingMode);
		
		this.userConstraints = userConstraints;
		assert(userConstraints != null);
		this.applyUserConstraints = true;

		this.excludeFromTMappings = excludeFromTMappings;
		assert(excludeFromTMappings != null);
		//this.applyExcludeFromTMappings = true;
		
		this.init(rootOntology, obdaModel, configuration, preferences);
	}
	/**
	 * extract version from {@link it.unibz.krdb.obda.utils.VersionInfo}, which is from the file {@code version.properties}
	 */
	private void extractVersion() {
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
		version = new Version(major, minor, patch, build);
	}

	public void flush() {
		prepared = false;
		
		super.flush();
		
		prepareReasoner();
		
	}
	
	@Deprecated // used in one test only
	public Quest getQuestInstance() {
		return questInstance;
	}
	
	public void setPreferences(QuestPreferences preferences) {
		this.preferences = preferences;
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

		final boolean bObtainFromOntology = preferences.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_ONTOLOGY);
		final boolean bObtainFromMappings = preferences.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_MAPPINGS);
		final String unfoldingMode = (String) preferences.getCurrentValue(QuestPreferences.ABOX_MODE);

		// pm.reasonerTaskStarted("Classifying...");
		// pm.reasonerTaskBusy();

		questInstance = new Quest(translatedOntologyMerge, obdaModel, preferences);

		if(this.applyUserConstraints)
			questInstance.setImplicitDBConstraints(userConstraints);
		
		//if( this.applyExcludeFromTMappings )
			questInstance.setExcludeFromTMappings(this.excludeFromTMappings);
		
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
				QuestStatement st = conn.createStatement();
				if (bObtainFromOntology) {
					// Retrieves the ABox from the ontology file.
					log.debug("Loading data from Ontology into the database");
					OWLAPI3ABoxIterator aBoxIter = new OWLAPI3ABoxIterator(importsClosure);
					EquivalentTriplePredicateIterator aBoxNormalIter = 
							new EquivalentTriplePredicateIterator(aBoxIter, questInstance.getReasoner());
					
					int count = st.insertData(aBoxNormalIter, 5000, 500);
					log.debug("Inserted {} triples from the ontology.", count);
				}
				if (bObtainFromMappings) {
					// Retrieves the ABox from the target database via mapping.
					log.debug("Loading data from Mappings into the database");

					OBDAModel obdaModelForMaterialization = questInstance.getOBDAModel();
					obdaModelForMaterialization.declareAll(translatedOntologyMerge.getVocabulary());
					
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
				
				questInstance.updateSemanticIndexMappings();
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

		try {

			OWLOntologyManager man = ontology.getOWLOntologyManager();
			Set<OWLOntology> closure = man.getImportsClosure(ontology);
			Ontology mergeOntology = OWLAPI3TranslatorUtility.mergeTranslateOntologies(closure);
			return mergeOntology;
		} catch (Exception e) {
			throw e;
		}
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
	
	@Override
	public String getReasonerName() {
		return "Quest";
	}

	@Override
	public FreshEntityPolicy getFreshEntityPolicy() {
		return FreshEntityPolicy.ALLOW;
	}

	/**
	 * Gets the IndividualNodeSetPolicy in use by this reasoner. The policy is
	 * set at reasoner creation time.
	 * 
	 * @return The policy.
	 */
	@Override
	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
		return IndividualNodeSetPolicy.BY_NAME;
	}

	/**
	 * Gets the version of this reasoner.
	 * 
	 * @return The version of this reasoner. Not <code>null</code>.
	 */
	public Version getReasonerVersion() {
		return version;
	}

	@Override
	protected void handleChanges(Set<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms) {
		prepared = false;
		prepareReasoner();
	}

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
		try {
			/*
			 * Compute the an ontology with the merge of all the vocabulary and
			 * axioms of the closure of the root ontology
			 */

			this.translatedOntologyMerge = loadOntologies(getRootOntology());

			classHierarchyInfo.computeHierarchy();
			objectPropertyHierarchyInfo.computeHierarchy();
			dataPropertyHierarchyInfo.computeHierarchy();

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

	public void precomputeInferences(InferenceType... inferenceTypes) throws ReasonerInterruptedException, TimeOutException,
			InconsistentOntologyException {
		// System.out.println("precomputeInferences");
		ensurePrepared();
		// prepareReasoner();
	}

	public boolean isPrecomputed(InferenceType inferenceType) {
		// return true;
		return prepared;
	}

	public Set<InferenceType> getPrecomputableInferenceTypes() {
		return CollectionFactory.createSet(InferenceType.CLASS_HIERARCHY, InferenceType.OBJECT_PROPERTY_HIERARCHY,
				InferenceType.DATA_PROPERTY_HIERARCHY);
	}

	private void throwExceptionIfInterrupted() {
		if (interrupted) {
			interrupted = false;
			throw new ReasonerInterruptedException();
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////

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
				Set<ClassExpression> disj = dda.getComponents();
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
				Set<ObjectPropertyExpression> props = dda.getComponents();
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
				Set<DataPropertyExpression> props = dda.getComponents();
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
			String propFunc = pfa.getPredicate().getName();
			String strQuery = String.format(strQueryFunc, propFunc, propFunc);
			
			boolean isConsistent = executeConsistencyQuery(strQuery);
			if (!isConsistent) {
				inconsistent = pfa;
				return false;
			}
		}
		
		for (DataPropertyExpression pfa : translatedOntologyMerge.getFunctionalDataProperties()) {
			String propFunc = pfa.getPredicate().getName();
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
		QuestStatement query;
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

	public boolean isSatisfiable(OWLClassExpression classExpression) throws ReasonerInterruptedException, TimeOutException,
			ClassExpressionNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
		return !classExpression.isAnonymous()
				&& !getEquivalentClasses(classExpression.asOWLClass()).contains(getDataFactory().getOWLNothing());
	}

	public Node<OWLClass> getUnsatisfiableClasses() throws ReasonerInterruptedException, TimeOutException {
		return OWLClassNode.getBottomNode();
	}

	public boolean isEntailed(OWLAxiom axiom) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
		return getRootOntology().containsAxiomIgnoreAnnotations(axiom, true);
	}

	public boolean isEntailed(Set<? extends OWLAxiom> axioms) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException,
			TimeOutException, AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
		for (OWLAxiom ax : axioms) {
			if (!getRootOntology().containsAxiomIgnoreAnnotations(ax, true)) {
				return false;
			}
		}
		return true;
	}

	public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
		return false;
	}

	public Node<OWLClass> getTopClassNode() {
		ensurePrepared();
		return classHierarchyInfo.getEquivalents(getDataFactory().getOWLThing());
	}

	public Node<OWLClass> getBottomClassNode() {
		ensurePrepared();
		return classHierarchyInfo.getEquivalents(getDataFactory().getOWLNothing());
	}

	public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		OWLClassNodeSet ns = new OWLClassNodeSet();
		if (!ce.isAnonymous()) {
			ensurePrepared();
			return classHierarchyInfo.getNodeHierarchyChildren(ce.asOWLClass(), direct, ns);
		}
		return ns;
	}

	public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce, boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		OWLClassNodeSet ns = new OWLClassNodeSet();
		if (!ce.isAnonymous()) {
			ensurePrepared();
			return classHierarchyInfo.getNodeHierarchyParents(ce.asOWLClass(), direct, ns);
		}
		return ns;
	}

	public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		if (!ce.isAnonymous()) {
			return classHierarchyInfo.getEquivalents(ce.asOWLClass());
		} else {
			return new OWLClassNode();
		}
	}

	public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
		ensurePrepared();
		OWLClassNodeSet nodeSet = new OWLClassNodeSet();
		if (!ce.isAnonymous()) {
			for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
				for (OWLDisjointClassesAxiom ax : ontology.getDisjointClassesAxioms(ce.asOWLClass())) {
					for (OWLClassExpression op : ax.getClassExpressions()) {
						if (!op.isAnonymous()) {
							nodeSet.addNode(getEquivalentClasses(op));
						}
					}
				}
			}
		}
		return nodeSet;
	}

	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
		ensurePrepared();
		return objectPropertyHierarchyInfo.getEquivalents(getDataFactory().getOWLTopObjectProperty());
	}

	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
		ensurePrepared();
		return objectPropertyHierarchyInfo.getEquivalents(getDataFactory().getOWLBottomObjectProperty());
	}

	public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		OWLObjectPropertyNodeSet ns = new OWLObjectPropertyNodeSet();
		ensurePrepared();
		return objectPropertyHierarchyInfo.getNodeHierarchyChildren(pe, direct, ns);
	}

	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		OWLObjectPropertyNodeSet ns = new OWLObjectPropertyNodeSet();
		ensurePrepared();
		return objectPropertyHierarchyInfo.getNodeHierarchyParents(pe, direct, ns);
	}

	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		OWLObjectPropertyNode nd = new OWLObjectPropertyNode();
		ensurePrepared();
		return objectPropertyHierarchyInfo.getEquivalents(pe);
	}

	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		return new OWLObjectPropertyNodeSet();
	}

	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		OWLObjectPropertyExpression inv = pe.getInverseProperty().getSimplified();
		return getEquivalentObjectProperties(inv);
	}

	public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {

		ensurePrepared();
		DefaultNodeSet<OWLClass> result = new OWLClassNodeSet();
		for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
			for (OWLObjectPropertyDomainAxiom axiom : ontology.getObjectPropertyDomainAxioms(pe)) {
				result.addNode(getEquivalentClasses(axiom.getDomain()));
				if (!direct) {
					result.addAllNodes(getSuperClasses(axiom.getDomain(), false).getNodes());
				}
			}

			for (OWLObjectPropertyExpression invPe : getInverseObjectProperties(pe).getEntities()) {
				for (OWLObjectPropertyRangeAxiom axiom : ontology.getObjectPropertyRangeAxioms(invPe)) {
					result.addNode(getEquivalentClasses(axiom.getRange()));
					if (!direct) {
						result.addAllNodes(getSuperClasses(axiom.getRange(), false).getNodes());
					}
				}
			}
		}
		return result;
	}

	public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression pe, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		DefaultNodeSet<OWLClass> result = new OWLClassNodeSet();
		for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
			for (OWLObjectPropertyRangeAxiom axiom : ontology.getObjectPropertyRangeAxioms(pe)) {
				result.addNode(getEquivalentClasses(axiom.getRange()));
				if (!direct) {
					result.addAllNodes(getSuperClasses(axiom.getRange(), false).getNodes());
				}
			}
			for (OWLObjectPropertyExpression invPe : getInverseObjectProperties(pe).getEntities()) {
				for (OWLObjectPropertyDomainAxiom axiom : ontology.getObjectPropertyDomainAxioms(invPe)) {
					result.addNode(getEquivalentClasses(axiom.getDomain()));
					if (!direct) {
						result.addAllNodes(getSuperClasses(axiom.getDomain(), false).getNodes());
					}
				}
			}
		}
		return result;
	}

	public Node<OWLDataProperty> getTopDataPropertyNode() {
		ensurePrepared();
		return dataPropertyHierarchyInfo.getEquivalents(getDataFactory().getOWLTopDataProperty());
	}

	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		ensurePrepared();
		return dataPropertyHierarchyInfo.getEquivalents(getDataFactory().getOWLBottomDataProperty());
	}

	public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		OWLDataPropertyNodeSet ns = new OWLDataPropertyNodeSet();
		return dataPropertyHierarchyInfo.getNodeHierarchyChildren(pe, direct, ns);
	}

	public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		OWLDataPropertyNodeSet ns = new OWLDataPropertyNodeSet();
		return dataPropertyHierarchyInfo.getNodeHierarchyParents(pe, direct, ns);
	}

	public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		return dataPropertyHierarchyInfo.getEquivalents(pe);
	}

	public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		DefaultNodeSet<OWLDataProperty> result = new OWLDataPropertyNodeSet();
		for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
			for (OWLDisjointDataPropertiesAxiom axiom : ontology.getDisjointDataPropertiesAxioms(pe.asOWLDataProperty())) {
				for (OWLDataPropertyExpression dpe : axiom.getPropertiesMinus(pe)) {
					if (!dpe.isAnonymous()) {
						result.addNode(dataPropertyHierarchyInfo.getEquivalents(dpe.asOWLDataProperty()));
						result.addAllNodes(getSubDataProperties(dpe.asOWLDataProperty(), false).getNodes());
					}
				}
			}
		}
		return result;
	}

	public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		DefaultNodeSet<OWLClass> result = new OWLClassNodeSet();
		for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
			for (OWLDataPropertyDomainAxiom axiom : ontology.getDataPropertyDomainAxioms(pe)) {
				result.addNode(getEquivalentClasses(axiom.getDomain()));
				if (!direct) {
					result.addAllNodes(getSuperClasses(axiom.getDomain(), false).getNodes());
				}
			}
		}
		return result;
	}

	public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		DefaultNodeSet<OWLClass> result = new OWLClassNodeSet();
		for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
			for (OWLClassAssertionAxiom axiom : ontology.getClassAssertionAxioms(ind)) {
				OWLClassExpression ce = axiom.getClassExpression();
				if (!ce.isAnonymous()) {
					result.addNode(classHierarchyInfo.getEquivalents(ce.asOWLClass()));
					if (!direct) {
						result.addAllNodes(getSuperClasses(ce, false).getNodes());
					}
				}
			}
		}
		return result;
	}

	public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		DefaultNodeSet<OWLNamedIndividual> result = new OWLNamedIndividualNodeSet();
		if (!ce.isAnonymous()) {
			OWLClass cls = ce.asOWLClass();
			Set<OWLClass> clses = new HashSet<OWLClass>();
			clses.add(cls);
			if (!direct) {
				clses.addAll(getSubClasses(cls, false).getFlattened());
			}
			for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
				for (OWLClass curCls : clses) {
					for (OWLClassAssertionAxiom axiom : ontology.getClassAssertionAxioms(curCls)) {
						OWLIndividual individual = axiom.getIndividual();
						if (!individual.isAnonymous()) {
							if (getIndividualNodeSetPolicy().equals(IndividualNodeSetPolicy.BY_SAME_AS)) {
								result.addNode(getSameIndividuals(individual.asOWLNamedIndividual()));
							} else {
								result.addNode(new OWLNamedIndividualNode(individual.asOWLNamedIndividual()));
							}
						}
					}
				}
			}
		}
		return result;
	}

	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual ind, OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		OWLNamedIndividualNodeSet result = new OWLNamedIndividualNodeSet();
		Node<OWLObjectPropertyExpression> inverses = getInverseObjectProperties(pe);
		for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
			for (OWLObjectPropertyAssertionAxiom axiom : ontology.getObjectPropertyAssertionAxioms(ind)) {
				if (!axiom.getObject().isAnonymous()) {
					if (axiom.getProperty().getSimplified().equals(pe.getSimplified())) {
						if (getIndividualNodeSetPolicy().equals(IndividualNodeSetPolicy.BY_SAME_AS)) {
							result.addNode(getSameIndividuals(axiom.getObject().asOWLNamedIndividual()));
						} else {
							result.addNode(new OWLNamedIndividualNode(axiom.getObject().asOWLNamedIndividual()));
						}
					}
				}
				// Inverse of pe
				if (axiom.getObject().equals(ind) && !axiom.getSubject().isAnonymous()) {
					OWLObjectPropertyExpression invPe = axiom.getProperty().getInverseProperty().getSimplified();
					if (!invPe.isAnonymous() && inverses.contains(invPe.asOWLObjectProperty())) {
						if (getIndividualNodeSetPolicy().equals(IndividualNodeSetPolicy.BY_SAME_AS)) {
							result.addNode(getSameIndividuals(axiom.getObject().asOWLNamedIndividual()));
						} else {
							result.addNode(new OWLNamedIndividualNode(axiom.getObject().asOWLNamedIndividual()));
						}
					}
				}

			}
		}
		// Could do other stuff like inspecting owl:hasValue restrictions
		return result;
	}

	/**
	 * Gets the data property values for the specified individual and data
	 * property.
	 * 
	 * @param ind
	 *            The individual that is the subject of the data property values
	 * @param pe
	 *            The data property whose values are to be retrieved for the
	 *            specified individual
	 * @return A set of <code>OWLLiteral</code>s containing literals such that
	 *         for each literal <code>l</code> in the set, either there is an
	 *         explicit data property assertion in the set of reasoner axioms
	 *         <code>DataPropertyAssertion(pe, ind, l)</code>, or, there is an
	 *         explicit data property assertion in the set of reasoner axioms
	 *         <code>DataPropertyAssertion(S, ind, l)</code> and the set of
	 *         reasoner axioms entails <code>SubDataPropertyOf(S, pe)</code>.
	 * @throws InconsistentOntologyException
	 *             if the imports closure of the root ontology is inconsistent
	 * @throws org.semanticweb.owlapi.reasoner.FreshEntitiesException
	 *             if the signature of the individual and property is not
	 *             contained within the signature of the imports closure of the
	 *             root ontology and the undeclared entity policy of this
	 *             reasoner is set to
	 *             {@link org.semanticweb.owlapi.reasoner.FreshEntityPolicy#DISALLOW}
	 *             .
	 * @throws ReasonerInterruptedException
	 *             if the reasoning process was interrupted for any particular
	 *             reason (for example if reasoning was cancelled by a client
	 *             process)
	 * @throws TimeOutException
	 *             if the reasoner timed out during a basic reasoning operation.
	 *             See {@link #getTimeOut()}.
	 */
	public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind, OWLDataProperty pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		Set<OWLLiteral> literals = new HashSet<OWLLiteral>();
		Set<OWLDataProperty> superProperties = getSuperDataProperties(pe, false).getFlattened();
		superProperties.addAll(getEquivalentDataProperties(pe).getEntities());
		for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
			for (OWLDataPropertyAssertionAxiom axiom : ontology.getDataPropertyAssertionAxioms(ind)) {
				if (superProperties.contains(axiom.getProperty().asOWLDataProperty())) {
					literals.add(axiom.getObject());
				}
			}
		}
		return literals;
	}

	public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		ensurePrepared();
		Set<OWLNamedIndividual> inds = new HashSet<OWLNamedIndividual>();
		Set<OWLSameIndividualAxiom> processed = new HashSet<OWLSameIndividualAxiom>();
		List<OWLNamedIndividual> stack = new ArrayList<OWLNamedIndividual>();
		stack.add(ind);
		while (!stack.isEmpty()) {
			OWLNamedIndividual currentInd = stack.remove(0);
			for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
				for (OWLSameIndividualAxiom axiom : ontology.getSameIndividualAxioms(currentInd)) {
					if (!processed.contains(axiom)) {
						processed.add(axiom);
						for (OWLIndividual i : axiom.getIndividuals()) {
							if (!i.isAnonymous()) {
								OWLNamedIndividual namedInd = i.asOWLNamedIndividual();
								if (!inds.contains(namedInd)) {
									inds.add(namedInd);
									stack.add(namedInd);
								}
							}
						}
					}
				}
			}
		}

		return new OWLNamedIndividualNode(inds);
	}

	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual ind) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		return new OWLNamedIndividualNodeSet();
	}

	/**
	 * Methods to get the empty concepts and roles in the ontology using the given mappings.
	 * It generates SPARQL queries to check for entities.
	 * @return QuestOWLEmptyEntitiesChecker class to get empty concepts and roles
	 * @throws Exception
	 */
	
	public QuestOWLEmptyEntitiesChecker getEmptyEntitiesChecker() throws Exception{
		QuestOWLEmptyEntitiesChecker empties = new QuestOWLEmptyEntitiesChecker(translatedOntologyMerge, owlconn);
		return empties;
	}
	
	protected OWLDataFactory getDataFactory() {
		return getRootOntology().getOWLOntologyManager().getOWLDataFactory();
	}

	public void dumpClassHierarchy(boolean showBottomNode) {
		dumpClassHierarchy(OWLClassNode.getTopNode(), 0, showBottomNode);
	}

	private void dumpClassHierarchy(Node<OWLClass> cls, int level, boolean showBottomNode) {
		if (!showBottomNode && cls.isBottomNode()) {
			return;
		}
		printIndent(level);
		OWLClass representative = cls.getRepresentativeElement();
//		System.out.println(getEquivalentClasses(representative));
		for (Node<OWLClass> subCls : getSubClasses(representative, true)) {
			dumpClassHierarchy(subCls, level + 1, showBottomNode);
		}
	}

	public void dumpObjectPropertyHierarchy(boolean showBottomNode) {
		dumpObjectPropertyHierarchy(OWLObjectPropertyNode.getTopNode(), 0, showBottomNode);
	}

	private void dumpObjectPropertyHierarchy(Node<OWLObjectPropertyExpression> cls, int level, boolean showBottomNode) {
		if (!showBottomNode && cls.isBottomNode()) {
			return;
		}
		printIndent(level);
		OWLObjectPropertyExpression representative = cls.getRepresentativeElement();
//		System.out.println(getEquivalentObjectProperties(representative));
		for (Node<OWLObjectPropertyExpression> subProp : getSubObjectProperties(representative, true)) {
			dumpObjectPropertyHierarchy(subProp, level + 1, showBottomNode);
		}
	}

	public void dumpDataPropertyHierarchy(boolean showBottomNode) {
		dumpDataPropertyHierarchy(OWLDataPropertyNode.getTopNode(), 0, showBottomNode);
	}

	private void dumpDataPropertyHierarchy(Node<OWLDataProperty> cls, int level, boolean showBottomNode) {
		if (!showBottomNode && cls.isBottomNode()) {
			return;
		}
		printIndent(level);
		OWLDataProperty representative = cls.getRepresentativeElement();
//		System.out.println(getEquivalentDataProperties(representative));
		for (Node<OWLDataProperty> subProp : getSubDataProperties(representative, true)) {
			dumpDataPropertyHierarchy(subProp, level + 1, showBottomNode);
		}
	}

	private void printIndent(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("    ");
		}
	}

	@Override
	public void close() throws Exception {
		dispose();
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ////
	// //// HierarchyInfo
	// ////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private abstract class HierarchyInfo<T extends OWLObject> {

		private RawHierarchyProvider<T> rawParentChildProvider;

		/**
		 * The entity that always appears in the top node in the hierarchy
		 */
		private T topEntity;

		/**
		 * The entity that always appears as the bottom node in the hierarchy
		 */
		private T bottomEntity;

		private Set<T> directChildrenOfTopNode = new HashSet<T>();

		private Set<T> directParentsOfBottomNode = new HashSet<T>();

		private NodeCache<T> nodeCache;

		private String name;

		private int classificationSize;

		public HierarchyInfo(String name, T topEntity, T bottomEntity, RawHierarchyProvider<T> rawParentChildProvider) {
			this.topEntity = topEntity;
			this.bottomEntity = bottomEntity;
			this.nodeCache = new NodeCache<T>(this);
			this.rawParentChildProvider = rawParentChildProvider;
			this.name = name;
		}

		public RawHierarchyProvider<T> getRawParentChildProvider() {
			return rawParentChildProvider;
		}

		/**
		 * Gets the set of relevant entities from the specified ontology
		 * 
		 * @param ont
		 *            The ontology
		 * @return A set of entities to be "classified"
		 */
		protected abstract Set<T> getEntities(OWLOntology ont);

		/**
		 * Creates a node for a given set of entities
		 * 
		 * @param cycle
		 *            The set of entities
		 * @return A node
		 */
		protected abstract DefaultNode<T> createNode(Set<T> cycle);

		protected abstract DefaultNode<T> createNode();

		/**
		 * Gets the set of relevant entities in a particular axiom
		 * 
		 * @param ax
		 *            The axiom
		 * @return The set of relevant entities in the signature of the
		 *         specified axiom
		 */
		protected abstract Set<? extends T> getEntitiesInSignature(OWLAxiom ax);

		private Set<T> getEntitiesInSignature(Set<OWLAxiom> axioms) {
			Set<T> result = new HashSet<T>();
			for (OWLAxiom ax : axioms) {
				result.addAll(getEntitiesInSignature(ax));
			}
			return result;
		}

		public void computeHierarchy() {
			// pm.reasonerTaskStarted("Computing " + name + " hierarchy");
			// pm.reasonerTaskBusy();
			nodeCache.clear();
			Map<T, Collection<T>> cache = new HashMap<T, Collection<T>>();
			Set<T> entities = new HashSet<T>();
			for (OWLOntology ont : getRootOntology().getImportsClosure()) {
				entities.addAll(getEntities(ont));
			}
			classificationSize = entities.size();
			// pm.reasonerTaskProgressChanged(0, classificationSize);
			updateForSignature(entities, cache);
			// pm.reasonerTaskStopped();
		}

		private void updateForSignature(Set<T> signature, Map<T, Collection<T>> cache) {
			HashSet<Set<T>> cyclesResult = new HashSet<Set<T>>();
			Set<T> processed = new HashSet<T>();
			nodeCache.clearTopNode();
			nodeCache.clearBottomNode();
			nodeCache.clearNodes(signature);

			directChildrenOfTopNode.removeAll(signature);

			Set<T> equivTopOrChildrenOfTop = new HashSet<T>();
			Set<T> equivBottomOrParentsOfBottom = new HashSet<T>();
			for (T entity : signature) {
				if (!processed.contains(entity)) {
					// pm.reasonerTaskProgressChanged(processed.size(),
					// signature.size());
					tarjan(entity, 0, new Stack<T>(), new HashMap<T, Integer>(), new HashMap<T, Integer>(), cyclesResult, processed,
							new HashSet<T>(), cache, equivTopOrChildrenOfTop, equivBottomOrParentsOfBottom);
					throwExceptionIfInterrupted();
				}
			}
			// Store new cycles
			for (Set<T> cycle : cyclesResult) {
				nodeCache.addNode(cycle);
			}

			directChildrenOfTopNode.addAll(equivTopOrChildrenOfTop);
			directChildrenOfTopNode.removeAll(nodeCache.getTopNode().getEntities());

			directParentsOfBottomNode.addAll(equivBottomOrParentsOfBottom);
			directParentsOfBottomNode.removeAll(nodeCache.getBottomNode().getEntities());

			// Now check that each found cycle has a proper parent an child
			for (Set<T> node : cyclesResult) {
				if (!node.contains(topEntity) && !node.contains(bottomEntity)) {
					boolean childOfTop = true;
					for (T element : node) {
						Collection<T> parents = rawParentChildProvider.getParents(element);
						parents.removeAll(node);
						parents.removeAll(nodeCache.getTopNode().getEntities());
						if (!parents.isEmpty()) {
							childOfTop = false;
							break;
						}
					}
					if (childOfTop) {
						directChildrenOfTopNode.addAll(node);
					}

					boolean parentOfBottom = true;
					for (T element : node) {
						Collection<T> children = rawParentChildProvider.getChildren(element);
						children.removeAll(node);
						children.removeAll(nodeCache.getBottomNode().getEntities());
						if (!children.isEmpty()) {
							parentOfBottom = false;
							break;
						}
					}
					if (parentOfBottom) {
						directParentsOfBottomNode.addAll(node);
					}
				}

			}

		}

		/**
		 * Processes the specified signature that represents the signature of
		 * potential changes
		 * 
		 * @param signature
		 *            The signature
		 */
		public void processChanges(Set<T> signature, Set<OWLAxiom> added, Set<OWLAxiom> removed) {
			updateForSignature(signature, null);
		}

		// ////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////////////////////////////////////////

		/**
		 * Applies the tarjan algorithm for a given entity. This computes the
		 * cycle that the entity is involved in (if any).
		 * 
		 * @param entity
		 *            The entity
		 * @param cache
		 *            A cache of children to parents - may be <code>null</code>
		 *            if no caching is to take place.
		 * @param childrenOfTop
		 *            A set of entities that have a raw parent that is the top
		 *            entity
		 * @param parentsOfBottom
		 *            A set of entities that have a raw parent that is the
		 *            bottom entity
		 */
		public void tarjan(T entity, int index, Stack<T> stack, Map<T, Integer> indexMap, Map<T, Integer> lowlinkMap, Set<Set<T>> result,
				Set<T> processed, Set<T> stackEntities, Map<T, Collection<T>> cache, Set<T> childrenOfTop, Set<T> parentsOfBottom) {
			throwExceptionIfInterrupted();
			if (processed.add(entity)) {
				Collection<T> rawChildren = rawParentChildProvider.getChildren(entity);
				if (rawChildren.isEmpty() || rawChildren.contains(bottomEntity)) {
					parentsOfBottom.add(entity);
				}
			}
			// pm.reasonerTaskProgressChanged(processed.size(),
			// classificationSize);
			indexMap.put(entity, index);
			lowlinkMap.put(entity, index);
			index = index + 1;
			stack.push(entity);
			stackEntities.add(entity);

			// Get the raw parents - cache if necessary
			Collection<T> rawParents = null;
			if (cache != null) {
				// We are therefore caching raw parents of children.
				rawParents = cache.get(entity);
				if (rawParents == null) {
					// Not in cache!
					rawParents = rawParentChildProvider.getParents(entity);
					// Note down if our entity is a
					if (rawParents.isEmpty() || rawParents.contains(topEntity)) {
						childrenOfTop.add(entity);
					}
					cache.put(entity, rawParents);

				}
			} else {
				rawParents = rawParentChildProvider.getParents(entity);
				// Note down if our entity is a
				if (rawParents.isEmpty() || rawParents.contains(topEntity)) {
					childrenOfTop.add(entity);
				}
			}

			for (T superEntity : rawParents) {
				if (!indexMap.containsKey(superEntity)) {
					tarjan(superEntity, index, stack, indexMap, lowlinkMap, result, processed, stackEntities, cache, childrenOfTop,
							parentsOfBottom);
					lowlinkMap.put(entity, Math.min(lowlinkMap.get(entity), lowlinkMap.get(superEntity)));
				} else if (stackEntities.contains(superEntity)) {
					lowlinkMap.put(entity, Math.min(lowlinkMap.get(entity), indexMap.get(superEntity)));
				}
			}
			if (lowlinkMap.get(entity).equals(indexMap.get(entity))) {
				Set<T> scc = new HashSet<T>();
				while (true) {
					T clsPrime = stack.pop();
					stackEntities.remove(clsPrime);
					scc.add(clsPrime);
					if (clsPrime.equals(entity)) {
						break;
					}
				}
				if (scc.size() > 1) {
					// We ADD a cycle
					result.add(scc);
				}
			}
		}

		// ////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////////////////////////////////////////

		public NodeSet<T> getNodeHierarchyChildren(T parent, boolean direct, DefaultNodeSet<T> ns) {
			Node<T> node = nodeCache.getNode(parent);

			if (node.isBottomNode()) {
				return ns;
			}

			Set<T> directChildren = new HashSet<T>();
			for (T equiv : node) {
				directChildren.addAll(rawParentChildProvider.getChildren(equiv));
				if (directParentsOfBottomNode.contains(equiv)) {
					ns.addNode(nodeCache.getBottomNode());
				}
			}
			directChildren.removeAll(node.getEntities());

			if (node.isTopNode()) {
				// Special treatment
				directChildren.addAll(directChildrenOfTopNode);
			}

			for (Node<T> childNode : nodeCache.getNodes(directChildren)) {
				ns.addNode(childNode);
			}

			if (!direct) {
				for (T child : directChildren) {
					getNodeHierarchyChildren(child, direct, ns);
				}
			}
			return ns;
		}

		public NodeSet<T> getNodeHierarchyParents(T child, boolean direct, DefaultNodeSet<T> ns) {
			Node<T> node = nodeCache.getNode(child);

			if (node.isTopNode()) {
				return ns;
			}

			Set<T> directParents = new HashSet<T>();
			for (T equiv : node) {
				directParents.addAll(rawParentChildProvider.getParents(equiv));
				if (directChildrenOfTopNode.contains(equiv)) {
					ns.addNode(nodeCache.getTopNode());
				}
			}
			directParents.removeAll(node.getEntities());

			if (node.isBottomNode()) {
				// Special treatment
				directParents.addAll(directParentsOfBottomNode);
			}

			for (Node<T> parentNode : nodeCache.getNodes(directParents)) {
				ns.addNode(parentNode);
			}

			if (!direct) {
				for (T parent : directParents) {
					getNodeHierarchyParents(parent, direct, ns);
				}
			}
			return ns;
		}

		public Node<T> getEquivalents(T element) {
			return nodeCache.getNode(element);
		}
	}

	private static class NodeCache<T extends OWLObject> {

		private HierarchyInfo<T> hierarchyInfo;

		private Node<T> topNode;

		private Node<T> bottomNode;

		private Map<T, Node<T>> map = new HashMap<T, Node<T>>();

		protected NodeCache(HierarchyInfo<T> hierarchyInfo) {
			this.hierarchyInfo = hierarchyInfo;
			clearTopNode();
			clearBottomNode();
		}

		public void addNode(Node<T> node) {
			for (T element : node.getEntities()) {
				map.put(element, node);
				if (element.isTopEntity()) {
					topNode = node;
				} else if (element.isBottomEntity()) {
					bottomNode = node;
				}
			}
		}

		public Set<Node<T>> getNodes(Set<T> elements) {
			Set<Node<T>> result = new HashSet<Node<T>>();
			for (T element : elements) {
				result.add(getNode(element));
			}
			return result;
		}

		public Set<T> getTopEntities() {
			return topNode.getEntities();
		}

		public Set<T> getBottomEntities() {
			return bottomNode.getEntities();
		}

		public Node<T> getNode(T containing) {
			Node<T> parentNode = map.get(containing);
			if (parentNode != null) {
				return parentNode;
			} else {
				return hierarchyInfo.createNode(Collections.singleton(containing));
			}
		}

		public void addNode(Set<T> elements) {
			addNode(hierarchyInfo.createNode(elements));
		}

		public Node<T> getTopNode() {
			return topNode;
		}

		public Node<T> getBottomNode() {
			return bottomNode;
		}

		public void setTopNode(Node<T> topNode) {
			this.topNode = topNode;
		}

		public void setBottomNode(Node<T> bottomNode) {
			this.bottomNode = bottomNode;
		}

		public void clearTopNode() {
			removeNode(hierarchyInfo.topEntity);
			topNode = hierarchyInfo.createNode(Collections.singleton(hierarchyInfo.topEntity));
			addNode(topNode);
		}

		public void clearBottomNode() {
			removeNode(hierarchyInfo.bottomEntity);
			bottomNode = hierarchyInfo.createNode(Collections.singleton(hierarchyInfo.bottomEntity));
			addNode(bottomNode);
		}

		public void clearNodes(Set<T> containing) {
			for (T entity : containing) {
				removeNode(entity);
			}
		}

		public void clear() {
			map.clear();
			clearTopNode();
			clearBottomNode();
		}

		public void removeNode(T containing) {
			Node<T> node = map.remove(containing);
			if (node != null) {
				for (T object : node.getEntities()) {
					map.remove(object);
				}
			}
		}
	}

	private class ClassHierarchyInfo extends HierarchyInfo<OWLClass> {

		private ClassHierarchyInfo() {
			super("class", getDataFactory().getOWLThing(), getDataFactory().getOWLNothing(), new RawClassHierarchyProvider());
		}

		@Override
		protected Set<OWLClass> getEntitiesInSignature(OWLAxiom ax) {
			return ax.getClassesInSignature();
		}

		@Override
		protected DefaultNode<OWLClass> createNode(Set<OWLClass> cycle) {
			return new OWLClassNode(cycle);
		}

		@Override
		protected Set<OWLClass> getEntities(OWLOntology ont) {
			return ont.getClassesInSignature();
		}

		@Override
		protected DefaultNode<OWLClass> createNode() {
			return new OWLClassNode();
		}
	}

	private class ObjectPropertyHierarchyInfo extends HierarchyInfo<OWLObjectPropertyExpression> {

		private ObjectPropertyHierarchyInfo() {
			super("object property", getDataFactory().getOWLTopObjectProperty(), getDataFactory().getOWLBottomObjectProperty(),
					new RawObjectPropertyHierarchyProvider());
		}

		@Override
		protected Set<OWLObjectPropertyExpression> getEntitiesInSignature(OWLAxiom ax) {
			Set<OWLObjectPropertyExpression> result = new HashSet<OWLObjectPropertyExpression>();
			for (OWLObjectProperty property : ax.getObjectPropertiesInSignature()) {
				result.add(property);
				result.add(property.getInverseProperty());
			}
			return result;
		}

		@Override
		protected Set<OWLObjectPropertyExpression> getEntities(OWLOntology ont) {
			Set<OWLObjectPropertyExpression> result = new HashSet<OWLObjectPropertyExpression>();
			for (OWLObjectPropertyExpression property : ont.getObjectPropertiesInSignature()) {
				result.add(property);
				result.add(property.getInverseProperty());
			}
			return result;
		}

		@Override
		protected DefaultNode<OWLObjectPropertyExpression> createNode(Set<OWLObjectPropertyExpression> cycle) {
			return new OWLObjectPropertyNode(cycle);
		}

		@Override
		protected DefaultNode<OWLObjectPropertyExpression> createNode() {
			return new OWLObjectPropertyNode();
		}

		/**
		 * Processes the specified signature that represents the signature of
		 * potential changes
		 * 
		 * @param signature
		 *            The signature
		 */
		@Override
		public void processChanges(Set<OWLObjectPropertyExpression> signature, Set<OWLAxiom> added, Set<OWLAxiom> removed) {
			boolean rebuild = false;
			for (OWLAxiom ax : added) {
				if (ax instanceof OWLObjectPropertyAxiom) {
					rebuild = true;
					break;
				}
			}
			if (!rebuild) {
				for (OWLAxiom ax : removed) {
					if (ax instanceof OWLObjectPropertyAxiom) {
						rebuild = true;
						break;
					}
				}
			}
			if (rebuild) {
				((RawObjectPropertyHierarchyProvider) getRawParentChildProvider()).rebuild();
			}
			super.processChanges(signature, added, removed);
		}
	}

	private class DataPropertyHierarchyInfo extends HierarchyInfo<OWLDataProperty> {

		private DataPropertyHierarchyInfo() {
			super("data property", getDataFactory().getOWLTopDataProperty(), getDataFactory().getOWLBottomDataProperty(),
					new RawDataPropertyHierarchyProvider());
		}

		@Override
		protected Set<OWLDataProperty> getEntitiesInSignature(OWLAxiom ax) {
			return ax.getDataPropertiesInSignature();
		}

		@Override
		protected Set<OWLDataProperty> getEntities(OWLOntology ont) {
			return ont.getDataPropertiesInSignature();
		}

		@Override
		protected DefaultNode<OWLDataProperty> createNode(Set<OWLDataProperty> cycle) {
			return new OWLDataPropertyNode(cycle);
		}

		@Override
		protected DefaultNode<OWLDataProperty> createNode() {
			return new OWLDataPropertyNode();
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * An interface for objects who can provide the parents and children of some
	 * object.
	 * 
	 * @param <T>
	 */
	private interface RawHierarchyProvider<T> {

		/**
		 * Gets the parents as asserted. These parents may also be children
		 * (resulting in equivalences).
		 * 
		 * @param child
		 *            The child whose parents are to be retrieved
		 * @return The raw asserted parents of the specified child. If the child
		 *         does not have any parents then the empty set can be returned.
		 */
		Collection<T> getParents(T child);

		/**
		 * Gets the children as asserted
		 * 
		 * @param parent
		 *            The parent whose children are to be retrieved
		 * @return The raw asserted children of the speicified parent
		 */
		Collection<T> getChildren(T parent);

	}

	private class RawClassHierarchyProvider implements RawHierarchyProvider<OWLClass> {

		public Collection<OWLClass> getParents(OWLClass child) {
			Collection<OWLClass> result = new HashSet<OWLClass>();
			for (OWLOntology ont : getRootOntology().getImportsClosure()) {
				for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(child)) {
					OWLClassExpression superCls = ax.getSuperClass();
					if (!superCls.isAnonymous()) {
						result.add(superCls.asOWLClass());
					} else if (superCls instanceof OWLObjectIntersectionOf) {
						OWLObjectIntersectionOf intersectionOf = (OWLObjectIntersectionOf) superCls;
						for (OWLClassExpression conjunct : intersectionOf.asConjunctSet()) {
							if (!conjunct.isAnonymous()) {
								result.add(conjunct.asOWLClass());
							}
						}
					}
				}
				for (OWLEquivalentClassesAxiom ax : ont.getEquivalentClassesAxioms(child)) {
					for (OWLClassExpression ce : ax.getClassExpressionsMinus(child)) {
						if (!ce.isAnonymous()) {
							result.add(ce.asOWLClass());
						} else if (ce instanceof OWLObjectIntersectionOf) {
							OWLObjectIntersectionOf intersectionOf = (OWLObjectIntersectionOf) ce;
							for (OWLClassExpression conjunct : intersectionOf.asConjunctSet()) {
								if (!conjunct.isAnonymous()) {
									result.add(conjunct.asOWLClass());
								}
							}
						}
					}
				}
			}
			return result;
		}

		public Collection<OWLClass> getChildren(OWLClass parent) {
			Collection<OWLClass> result = new HashSet<OWLClass>();
			for (OWLOntology ont : getRootOntology().getImportsClosure()) {
				for (OWLAxiom ax : ont.getReferencingAxioms(parent)) {
					if (ax instanceof OWLSubClassOfAxiom) {
						OWLSubClassOfAxiom sca = (OWLSubClassOfAxiom) ax;
						if (!sca.getSubClass().isAnonymous()) {
							Set<OWLClassExpression> conjuncts = sca.getSuperClass().asConjunctSet();
							if (conjuncts.contains(parent)) {
								result.add(sca.getSubClass().asOWLClass());
							}
						}
					} else if (ax instanceof OWLEquivalentClassesAxiom) {
						OWLEquivalentClassesAxiom eca = (OWLEquivalentClassesAxiom) ax;
						for (OWLClassExpression ce : eca.getClassExpressions()) {
							if (ce.containsConjunct(parent)) {
								for (OWLClassExpression sub : eca.getClassExpressions()) {
									if (!sub.isAnonymous() && !sub.equals(ce)) {
										result.add(sub.asOWLClass());
									}
								}
							}
						}
					}
				}
			}
			return result;
		}
	}

	private class RawObjectPropertyHierarchyProvider implements RawHierarchyProvider<OWLObjectPropertyExpression> {

		private OWLObjectPropertyManager propertyManager;

		private Map<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>> sub2Super;

		private Map<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>> super2Sub;

		private RawObjectPropertyHierarchyProvider() {
			rebuild();
		}

		public void rebuild() {
			propertyManager = new OWLObjectPropertyManager(getRootOntology().getOWLOntologyManager(), getRootOntology());
			sub2Super = propertyManager.getPropertyHierarchy();
			super2Sub = new HashMap<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>>();
			for (OWLObjectPropertyExpression sub : sub2Super.keySet()) {
				for (OWLObjectPropertyExpression superProp : sub2Super.get(sub)) {
					Set<OWLObjectPropertyExpression> subs = super2Sub.get(superProp);
					if (subs == null) {
						subs = new HashSet<OWLObjectPropertyExpression>();
						super2Sub.put(superProp, subs);
					}
					subs.add(sub);
				}
			}
		}

		public Collection<OWLObjectPropertyExpression> getParents(OWLObjectPropertyExpression child) {
			if (child.isBottomEntity()) {
				return Collections.emptySet();
			}
			Set<OWLObjectPropertyExpression> propertyExpressions = sub2Super.get(child);
			if (propertyExpressions == null) {
				return Collections.emptySet();
			} else {
				return new HashSet<OWLObjectPropertyExpression>(propertyExpressions);
			}

		}

		public Collection<OWLObjectPropertyExpression> getChildren(OWLObjectPropertyExpression parent) {
			if (parent.isTopEntity()) {
				return Collections.emptySet();
			}
			Set<OWLObjectPropertyExpression> propertyExpressions = super2Sub.get(parent);
			if (propertyExpressions == null) {
				return Collections.emptySet();
			} else {
				return new HashSet<OWLObjectPropertyExpression>(propertyExpressions);
			}

		}
	}

	private class RawDataPropertyHierarchyProvider implements RawHierarchyProvider<OWLDataProperty> {

		public Collection<OWLDataProperty> getParents(OWLDataProperty child) {
			Set<OWLDataProperty> properties = new HashSet<OWLDataProperty>();
			for (OWLDataPropertyExpression prop : child.getSuperProperties(getRootOntology().getImportsClosure())) {
				properties.add(prop.asOWLDataProperty());
			}
			return properties;
		}

		public Collection<OWLDataProperty> getChildren(OWLDataProperty parent) {
			Set<OWLDataProperty> properties = new HashSet<OWLDataProperty>();
			for (OWLDataPropertyExpression prop : parent.getSubProperties(getRootOntology().getImportsClosure())) {
				properties.add(prop.asOWLDataProperty());
			}
			return properties;
		}
	}

}
