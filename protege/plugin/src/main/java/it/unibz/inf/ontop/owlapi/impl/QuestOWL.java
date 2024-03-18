package it.unibz.inf.ontop.owlapi.impl;

import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.query.AskQuery;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.query.resultset.BooleanResultSet;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.impl.DefaultOntopOWLConnection;
import it.unibz.inf.ontop.shaded.com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.shaded.com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.VersionInfo;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLReasonerBase;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The OBDAOWLReformulationPlatform implements the OWL reasoner interface and is
 * the implementation of the reasoning method in the reformulation project.
 */
public class QuestOWL extends OWLReasonerBase implements OntopOWLReasoner {

	private final StructuralReasoner structuralReasoner;

	private final Version version;

	private final ReasonerProgressMonitor pm;

	private boolean prepared = false;
	private boolean isOntopConnectionInitialized = false;

	private boolean questready = false;

	private Object inconsistent = null;

	private Exception questException = null;

	private final ClassifiedTBox classifiedTBox;

	private static final Logger log = LoggerFactory.getLogger(QuestOWL.class);


	/* Used to enable querying annotation Properties coming from the ontology. */

	/* Used to enable use of same as in mappings. */

	private final OntopQueryEngine queryEngine;
	private final KGQueryFactory kgQueryFactory;

	/*
	 * Used to handle conversion to and from OWL API and Internal APIs in order to
	 * support reasoning capabilities via the classifiedTBox.
	 * */
	private final OWLOntologyManager owlOntologyManager = getRootOntology().getOWLOntologyManager();
	private final OWLDataFactory owlDataFactory = getOWLDataFactory();
	private final RDF rdfFactory = new RDF4J();

	/**
	 * End-users: use the QuestOWLFactory instead
	 */
	protected QuestOWL(OWLOntology rootOntology, QuestOWLConfiguration owlConfiguration)
			throws IllegalConfigurationException {
		super(rootOntology, owlConfiguration, BufferingMode.BUFFERING);

		OntopSystemConfiguration ontopConfiguration = owlConfiguration.getOntopConfiguration();

		/*
		 * Validates the preferences
		 */
		try {
			ontopConfiguration.validate();
		} catch (InvalidOntopConfigurationException e) {
			throw new IllegalConfigurationException(e.getMessage(), e, owlConfiguration);
		}

		this.structuralReasoner = new StructuralReasoner(rootOntology, owlConfiguration, BufferingMode.BUFFERING);

		try {
			OBDASpecification obdaSpecification = ontopConfiguration.loadSpecification();
			classifiedTBox = obdaSpecification.getSaturatedTBox();
			Injector injector = ontopConfiguration.getInjector();
			OntopSystemFactory systemFactory = injector.getInstance(OntopSystemFactory.class);
			queryEngine = systemFactory.create(obdaSpecification);
			/*
			 * Mapping parsing exceptions are re-thrown as configuration exceptions.
			 */
		} catch (OBDASpecificationException e) {
			throw new IllegalConfigurationException(e.getMessage(), e, owlConfiguration);
		}

		kgQueryFactory = ontopConfiguration.getKGQueryFactory();

		pm = owlConfiguration.getProgressMonitor();

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

	private void prepareConnector() throws OntopConnectionException {

		if (!isOntopConnectionInitialized) {
			log.info("Initializing a new Ontop instance...");
			queryEngine.connect();
			isOntopConnectionInitialized = true;
		}

		// pm.reasonerTaskStarted("Classifying...");
		// pm.reasonerTaskBusy();

		// Set<OWLOntology> importsClosure = man.getImportsClosure(getRootOntology());

		questready = true;

	}

	@Override
	public void dispose() {
		super.dispose();
		try {
			queryEngine.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	/**
	 * The caller is in charge of closing the connection after usage.
	 *    (the reasoner is not responsible of connections)
	 */
	@Override
	public OntopOWLConnection getConnection() throws ReasonerInternalException {
		if (!questready) {
			OWLReasonerRuntimeException owlReasonerRuntimeException = new ReasonerInternalException(
					"Ontop was not initialized properly. This is generally indicates, " +
							"connection problems or error during ontology or mapping pre-processing. " +
							"\n\nOriginal error message:\n" + questException.getMessage()) ;
			owlReasonerRuntimeException.setStackTrace(questException.getStackTrace());
			throw owlReasonerRuntimeException;
		}

		try {
			OntopConnection conn = queryEngine.getConnection();
			return new DefaultOntopOWLConnection(conn, kgQueryFactory);
		}
		catch (OntopConnectionException e) {
			// TODO: find a better exception?
			throw new ReasonerInternalException(e);
		}
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
		/* interrupted = true; */
	}

	private void ensurePrepared() {
		if (!prepared) {
			prepareReasoner();
		}
	}

	private void prepareReasoner() throws ReasonerInterruptedException, TimeOutException {
		pm.reasonerTaskStarted("Classifying...");
		pm.reasonerTaskBusy();

		structuralReasoner.prepareReasoner();

		try {
			questready = false;
			questException = null;
			try {
				prepareConnector();
				questready = true;
				questException = null;
			} catch (Exception e) {
				questready = false;
				questException = e;
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
	public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
		try {
			return isDisjointAxiomsConsistent() && isFunctionalPropertyAxiomsConsistent();
		} catch (OWLException e) {
			throw new ReasonerInterruptedException(e);
		}
	}

	//info to return which axiom was inconsistent during the check
	@Override
	public Object getInconsistentAxiom() {
		return inconsistent;
	}

	private boolean isDisjointAxiomsConsistent() throws ReasonerInterruptedException, TimeOutException, OWLException {

		//deal with disjoint classes
		{
			final String strQueryClass = "ASK {?x a <%s>; a <%s> }";

			for (NaryAxiom<ClassExpression> dda : classifiedTBox.disjointClasses()) {
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
					: classifiedTBox.disjointObjectProperties()) {
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
					: classifiedTBox.disjointDataProperties()) {
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

	private boolean isFunctionalPropertyAxiomsConsistent() throws ReasonerInterruptedException, TimeOutException, OWLException {

		//deal with functional properties

		final String strQueryFunc = "ASK { ?x <%s> ?y; <%s> ?z. FILTER (?z != ?y) }";

		for (ObjectPropertyExpression pfa : classifiedTBox.functionalObjectProperties()) {
			// TODO: handle inverses
			String propFunc = pfa.getIRI().getIRIString();
			String strQuery = String.format(strQueryFunc, propFunc, propFunc);

			boolean isConsistent = executeConsistencyQuery(strQuery);
			if (!isConsistent) {
				inconsistent = pfa;
				return false;
			}
		}

		for (DataPropertyExpression pfa : classifiedTBox.functionalDataProperties()) {
			String propFunc = pfa.getIRI().getIRIString();
			String strQuery = String.format(strQueryFunc, propFunc, propFunc);

			boolean isConsistent = executeConsistencyQuery(strQuery);
			if (!isConsistent) {
				inconsistent = pfa;
				return false;
			}
		}

		return true;
	}

	private boolean executeConsistencyQuery(String strQuery) throws OWLException {
		try (OntopConnection connection = queryEngine.getConnection();
			 OntopStatement st = connection.createStatement()) {

			AskQuery query = kgQueryFactory.createAskQuery(strQuery);
			BooleanResultSet trs = st.execute(query);
			if (trs != null) {
				boolean b = trs.getValue();
				trs.close();
				return !b;
			}
			return true;
		} catch (Exception e) {
			throw new OWLException(e);
		}
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
		OntologyVocabularyCategory<OClass> oClasses = classifiedTBox.classes();
		OWLClass nothing = owlDataFactory.getOWLNothing();

		Set<OWLClass> bottoms = new HashSet<>();
		//bottoms.add(nothing);

		//TODO: ClassifiedTBox implementation may be incomplete - must go through this with supervisor
		for (OClass oClass : oClasses) {
			boolean isBottom = oClass.isBottom();

			if (isBottom) {
				String iriString = oClass.getIRI().getIRIString();
				org.semanticweb.owlapi.model.IRI OWLiri = org.semanticweb.owlapi.model.IRI.create(iriString);
				bottoms.add(owlDataFactory.getOWLClass(OWLiri));
				//bottomClass = owlDataFactory.getOWLClass(OWLiri);

				//return new OWLClassNode(bottomClass);
			}
		}

		return new OWLClassNode(bottoms);
//		return structuralReasoner.getBottomClassNode();
	}

	@Nonnull
	@Override
	public NodeSet<OWLClass> getSubClasses(@Nonnull OWLClassExpression ce, boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		switch (ce.getClassExpressionType()) {
			case OWL_CLASS:
				OWLClass owlClass = ce.asOWLClass();
				OClass oClass = owlClassAsOClass(owlClass);
				OWLClassNodeSet nodes = getSubClassesFromClassExpression(oClass);

				// TODO: Discuss with Roman how this should be implemented - directly in the ClassifiedTBox impl.?
//				if (!direct) {
//					nodes.addEntity(owlDataFactory.getOWLNothing());
//				}

				return nodes;
			case OBJECT_SOME_VALUES_FROM:
				OWLObjectSomeValuesFrom owlSomeValuesFrom = (OWLObjectSomeValuesFrom) ce;

				OWLObjectPropertyExpression propertyExpression = owlSomeValuesFrom.getProperty();
				OWLClassExpression filler = owlSomeValuesFrom.getFiller();

				if (filler.isOWLThing()) {
					OWLObjectProperty property = propertyExpression.getNamedProperty();
					ObjectPropertyExpression objectPropertyExpression = owlObjectPropertyAsObjectPropertyExpression(property);

					// This is another way to maybe do it?
//					property.getInverseProperty().getNamedProperty();

					if(propertyExpression.isAnonymous()) {
						objectPropertyExpression = objectPropertyExpression.getInverse();
					}

					ObjectSomeValuesFrom oSomeValuesFrom = objectPropertyExpression.getDomain();
					OWLClassNodeSet nodes1 = getSubClassesFromClassExpression(oSomeValuesFrom);

//					if (propertyExpression.isNamed()) {
//						// propertyExpression = property
//						ObjectSomeValuesFrom oSomeValuesFrom = objectPropertyExpression.getDomain();
//						Equivalences<ClassExpression> objectSomeValuesFromEquivalences = new Equivalences<>(ImmutableSet.of(oSomeValuesFrom));
//						ImmutableSet<Equivalences<ClassExpression>> sub = classifiedTBox.classesDAG().getSub(objectSomeValuesFromEquivalences);
////						Set<OWLClassNode> collect = objectPropSub.stream()
////								.flatMap(x -> x.getMembers().stream())
////								.filter(x -> x instanceof OClass)
////								.map(x -> (OClass) x)
////								.map(x -> x.getIRI().getIRIString())
////								.map(x -> owlDataFactory.getOWLClass(org.semanticweb.owlapi.model.IRI.create(x)))
////								.map(OWLClassNode::new)
////								.collect(Collectors.toSet());
//					} else {
//						// propertyExpression = inv(property)
//						objectPropertyExpression = objectPropertyExpression.getInverse();
//
//						// Do the same thing as the above block?
//					}

				} else {
					return new OWLClassNodeSet();
				}
			default:
				return new OWLClassNodeSet();
		}
//		switch (ce.getClassExpressionType()) {
//			case OWL_CLASS:
//				//return structuralReasoner.getSubClasses(ce, direct);
//			case OBJECT_SOME_VALUES_FROM:
//				OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) ce;
//				OWLObjectPropertyExpression property = someValuesFrom.getProperty();
//				OWLClassExpression filler = someValuesFrom.getFiller();
//				if(filler.isOWLThing()) {
//					//return structuralReasoner.getSubClasses(ce, direct);
//				}
//				break;
//			default:
//				break;
//		}

		//return structuralReasoner.getSubClasses(ce, direct);
	}

	@Nonnull
	@Override
	public NodeSet<OWLClass> getSuperClasses(@Nonnull OWLClassExpression ce, boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		switch (ce.getClassExpressionType()) {
			case OWL_CLASS:
				OWLClass owlClass = ce.asOWLClass();
				OClass oClass = owlClassAsOClass(owlClass);
				return getSuperClassesFromClassExpression(oClass);
			case OBJECT_SOME_VALUES_FROM:
				return new OWLClassNodeSet();
			default:
				return new OWLClassNodeSet();
		}
	}

	@Nonnull
	@Override
	public Node<OWLClass> getEquivalentClasses(@Nonnull OWLClassExpression ce) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// Will use this later to get the equivalent classes from the classifiedTBox
		EquivalencesDAG<ClassExpression> classesDag = classifiedTBox.classesDAG();


		// Convert ClassExpression to a Class, then get the IRI as a String.
		// Allows us to create an IRI object with rdf4j commons rdf api, compliant with what the TBox impl uses.
		String iriString = ce.asOWLClass().getIRI().toString();
		IRI iri = rdfFactory.createIRI(iriString);

		// Get the OClass from the classifiedTBox with our new IRI. OClass inherits from ClassExpression.
		// We have essentially converted from OWLCassExpression to ClassExpression.
		OClass oClass = classifiedTBox.classes().get(iri);

		// The above code can be replaced with my newly created private utility function


		//Get the equivalences for the OClass.
		Equivalences<ClassExpression> equivalences = classesDag.getVertex(oClass);

		Set<OWLClass> collect = equivalences.getMembers().stream()
				.filter(x -> x != oClass)
				.filter(x -> x instanceof OClass)
				.map(y -> oClassAsOWLClass((OClass) y))
				.collect(Collectors.toSet());

		return new OWLClassNode(collect);
	}

	@Nonnull
	@Override
	public NodeSet<OWLClass> getDisjointClasses(@Nonnull OWLClassExpression ce) {
		ImmutableList<NaryAxiom<ClassExpression>> naryAxioms = classifiedTBox.disjointClasses();

		switch (ce.getClassExpressionType()) {
			case OWL_CLASS:
				// I think this implementation is complete.
				OClass oClass = owlClassAsOClass(ce.asOWLClass());

				Set<Node<OWLClass>> classes;
				classes = naryAxioms.stream()
						.filter(x -> x.getComponents().contains(oClass))
						.flatMap(x -> x.getComponents().stream()
								.filter(y -> y != oClass)
								.map(y -> oClassAsOWLClass((OClass) y))
								.map(OWLClassNode::new))
						.collect(Collectors.toSet());

				OWLClassNodeSet nodes = new OWLClassNodeSet(classes);
				return nodes;
			case OBJECT_SOME_VALUES_FROM:
			default:
				return new OWLClassNodeSet();
		}
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

	@Override
	public void close() {
		dispose();
	}

//	private OWLObjectSomeValuesFrom owlObjectSomeValuesFromAsOWLObjectSomeValuesFrom(OWLObjectSomeValuesFrom someValuesFrom) {
//		OWLObjectPropertyExpression property = someValuesFrom.getProperty();
//		OWLClassExpression filler = someValuesFrom.getFiller();
//		return owlDataFactory.getOWLObjectSomeValuesFrom(property, filler);
//	}

//	private ObjectSomeValuesFrom owlObjectSomeValuesFromAsObjectSomeValuesFrom(OWLObjectSomeValuesFrom someValuesFrom) {
//		OWLObjectPropertyExpression property = someValuesFrom.getProperty();
//		OWLClassExpression filler = someValuesFrom.getFiller();
//		return classifiedTBox.objectSomeValuesFrom(property, filler);
//	}

	private OClass owlClassAsOClass(OWLClass owlClass) {
		String iriString = owlClass.getIRI().toString();
		IRI iri = rdfFactory.createIRI(iriString);
		return classifiedTBox.classes().get(iri);
	}

	private OWLClass oClassAsOWLClass(OClass oClass) {
		String iriString = oClass.getIRI().getIRIString();
		return owlDataFactory.getOWLClass(org.semanticweb.owlapi.model.IRI.create(iriString));
	}

	private ObjectPropertyExpression owlObjectPropertyAsObjectPropertyExpression(OWLObjectProperty property) {
		String iriString = property.getIRI().toString();
		IRI iri = rdfFactory.createIRI(iriString);
		return classifiedTBox.objectProperties().get(iri);
	}

	private OWLClassNodeSet getSubClassesFromClassExpression(ClassExpression ce) {
		Equivalences<ClassExpression> equivalences = new Equivalences<>(ImmutableSet.of(ce));
		ImmutableSet<Equivalences<ClassExpression>> subClasses = classifiedTBox.classesDAG().getSub(equivalences);

		Set<Node<OWLClass>> classes;
		classes = subClasses.stream()
				.flatMap(x -> x.getMembers().stream()) //
				.filter(y -> y != ce)
				.filter(x -> x instanceof OClass)
				.map(x -> oClassAsOWLClass((OClass) x))
				.map(OWLClassNode::new)
				.collect(Collectors.toSet());
		return new OWLClassNodeSet(classes);
	}

	private OWLClassNodeSet getSuperClassesFromClassExpression(ClassExpression ce) {
		Equivalences<ClassExpression> equivalences = new Equivalences<>(ImmutableSet.of(ce));
		ImmutableSet<Equivalences<ClassExpression>> subClasses = classifiedTBox.classesDAG().getSuper(equivalences);

		Set<Node<OWLClass>> classes;
		classes = subClasses.stream()
				.flatMap(x -> x.getMembers().stream()) //
				.filter(y -> y != ce)
				.filter(x -> x instanceof OClass)
				.map(x -> oClassAsOWLClass((OClass) x))
				.map(OWLClassNode::new)
				.collect(Collectors.toSet());
		return new OWLClassNodeSet(classes);
	}


//	private ClassExpression objectPropertyExpressionAsClassExpression(ObjectPropertyExpression property) {
//		String iriString = property.getIRI().getIRIString();
//		return classifiedTBox.classes().get(iriString);
//	}

//	private ObjectSomeValuesFrom owlObjectSomeValuesFromAsObjectSomeValuesFrom(OWLObjectSomeValuesFrom someValuesFrom) {
//		OWLObjectPropertyExpression property = someValuesFrom.getProperty();
//		OWLClassExpression filler = someValuesFrom.getFiller();
//		return classifiedTBox.objectSomeValuesFrom(property, filler);
//	}
}
