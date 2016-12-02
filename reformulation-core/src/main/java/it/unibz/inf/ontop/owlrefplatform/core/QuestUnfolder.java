package it.unibz.inf.ontop.owlrefplatform.core;


import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.VocabularyValidator;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.MappingDataTypeRepair;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.MappingSameAs;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingProcessor;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.DatalogUnfolder;

import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.impl.TermUtils;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;
import it.unibz.inf.ontop.pivotalrepr.impl.MetadataForQueryOptimizationImpl;
import it.unibz.inf.ontop.sql.DBMetadata;
import it.unibz.inf.ontop.utils.IMapping2DatalogConverter;
import net.sf.jsqlparser.JSQLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class QuestUnfolder {

	private final NativeQueryLanguageComponentFactory nativeQLFactory;
	/* The active unfolding engine */
	private DatalogUnfolder unfolder;

	/*
	 * These are pattern matchers that will help transforming the URI's in
	 * queries into Functions, used by the SPARQL translator.
	 */
	private UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher();

	protected List<CQIE> ufp; // for TESTS ONLY

	private static final Logger log = LoggerFactory.getLogger(QuestUnfolder.class);
	
	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	private ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> uniqueConstraints;
	private final IMapping2DatalogConverter mapping2DatalogConvertor;
	private MetadataForQueryOptimization metadataForQueryOptimization;

	private Set<Predicate> dataPropertiesAndClassesMapped = new HashSet<>();
	private Set<Predicate> objectPropertiesMapped = new HashSet<>();

	/** Davide> Whether to exclude the user-supplied predicates from the
	 *          TMapping procedure (that is, the mapping assertions for
	 *          those predicates should not be extended according to the
	 *          TBox hierarchies
	 */
	//private boolean applyExcludeFromTMappings = false;
	public QuestUnfolder(NativeQueryLanguageComponentFactory nativeQLFactory,
						 IMapping2DatalogConverter mapping2DatalogConvertor) throws Exception{
		this.nativeQLFactory = nativeQLFactory;
		this.mapping2DatalogConvertor = mapping2DatalogConvertor;
	}


    /**
	 * Setting up the unfolder and SQL generation
	 */

	public void setupInVirtualMode(Collection<OBDAMappingAxiom> mappingAxioms, DataSourceMetadata metadata,
								   DBConnector dbConnector,
								   VocabularyValidator vocabularyValidator, TBoxReasoner reformulationReasoner,
								   Ontology inputOntology, TMappingExclusionConfig excludeFromTMappings,
								   boolean queryingAnnotationsInOntology, boolean sameAs)
			throws SQLException, JSQLParserException, OBDAException {

		mappingAxioms = vocabularyValidator.replaceEquivalences(mappingAxioms);

		Collection<OBDAMappingAxiom> normalizedMappingAxioms = dbConnector.applyDBSpecificNormalization(mappingAxioms,
				metadata);

		List<CQIE> unfoldingProgram = mapping2DatalogConvertor.constructDatalogProgram(normalizedMappingAxioms, metadata);


		log.debug("Original mapping size: {}", unfoldingProgram.size());

		// Normalizing language tags and equalities
		normalizeMappings(unfoldingProgram);

		// Apply TMappings
		unfoldingProgram = applyTMappings(unfoldingProgram, reformulationReasoner, true, metadata, dbConnector, excludeFromTMappings);

		// Adding ontology assertions (ABox) as rules (facts, head with no body).
		addAssertionsAsFacts(unfoldingProgram, inputOntology.getClassAssertions(),
				inputOntology.getObjectPropertyAssertions(), inputOntology.getDataPropertyAssertions());

		// Adding data typing on the mapping axioms.
		 // Adding NOT NULL conditions to the variables used in the head
		 // of all mappings to preserve SQL-RDF semantics
		extendTypesWithMetadata(unfoldingProgram, reformulationReasoner, vocabularyValidator, metadata);
		addNOTNULLToMappings(unfoldingProgram);

		// Collecting URI templates
		uriTemplateMatcher = UriTemplateMatcher.create(unfoldingProgram);

		// Adding ontology assertions (ABox) as rules (facts, head with no body).
		List<AnnotationAssertion> annotationAssertions;
		if (queryingAnnotationsInOntology) {
			annotationAssertions = inputOntology.getAnnotationAssertions();
		}
		else{
			annotationAssertions = Collections.emptyList();
		}
		addAssertionsAsFacts(unfoldingProgram, inputOntology.getClassAssertions(),
				inputOntology.getObjectPropertyAssertions(), inputOntology.getDataPropertyAssertions(), annotationAssertions);

		if (sameAs) {
			addSameAsMapping(unfoldingProgram);
		}

		// Adding "triple(x,y,z)" mappings for support of unbounded
		// predicates and variables as class names (implemented in the
		// sparql translator)
		unfoldingProgram.addAll(generateTripleMappings(unfoldingProgram));

		log.debug("Final set of mappings: \n {}", Joiner.on("\n").join(unfoldingProgram));

		Multimap<Predicate, List<Integer>> pkeys = dbConnector.extractUniqueConstraints(metadata);
		uniqueConstraints = convertUniqueConstraints(pkeys);
		metadataForQueryOptimization = new MetadataForQueryOptimizationImpl(uniqueConstraints, uriTemplateMatcher);
		unfolder = new DatalogUnfolder(unfoldingProgram, pkeys);
		
		this.ufp = unfoldingProgram;
	}

	public void setupInSemanticIndexMode(Collection<OBDAMappingAxiom> mappings,
										 DBConnector dbConnector,
										 TBoxReasoner reformulationReasoner,
										 DataSourceMetadata metadata) throws OBDAException {


		List<CQIE> unfoldingProgram = mapping2DatalogConvertor.constructDatalogProgram(mappings,
				metadata);

		// this call is required to complete the T-mappings by rules taking account of
		// existential quantifiers and inverse roles
		unfoldingProgram = applyTMappings(unfoldingProgram, reformulationReasoner, false, metadata,
				dbConnector, TMappingExclusionConfig.empty());

		// Collecting URI templates
		uriTemplateMatcher = UriTemplateMatcher.create(unfoldingProgram);

		// Adding "triple(x,y,z)" mappings for support of unbounded
		// predicates and variables as class names (implemented in the
		// sparql translator)
		unfoldingProgram.addAll(generateTripleMappings(unfoldingProgram));

		log.debug("Final set of mappings: \n {}", Joiner.on("\n").join(unfoldingProgram));


		Multimap<Predicate, List<Integer>> pkeys = dbConnector.extractUniqueConstraints(metadata);
		uniqueConstraints = convertUniqueConstraints(pkeys);
		metadataForQueryOptimization = new MetadataForQueryOptimizationImpl(uniqueConstraints, uriTemplateMatcher);
		unfolder = new DatalogUnfolder(unfoldingProgram, pkeys);

		this.ufp = unfoldingProgram;
	}

	/**
	 * Normalize language tags (make them lower-case) and equalities
	 * (remove them by replacing all equivalent terms with one representative)
	 */

	private void normalizeMappings(List<CQIE> unfoldingProgram) {

		// Normalizing language tags. Making all LOWER CASE

		for (CQIE mapping : unfoldingProgram) {
			Function head = mapping.getHead();
			for (Term term : head.getTerms()) {
				if (!(term instanceof Function))
					continue;

				Function typedTerm = (Function) term;
				if (typedTerm.getTerms().size() == 2 && typedTerm.getFunctionSymbol().getName().equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
					// changing the language, its always the second inner term (literal,lang)
					Term originalLangTag = typedTerm.getTerm(1);
					if (originalLangTag instanceof ValueConstant) {
						ValueConstant originalLangConstant = (ValueConstant) originalLangTag;
						Term normalizedLangTag = fac.getConstantLiteral(originalLangConstant.getValue().toLowerCase(),
								originalLangConstant.getType());
						typedTerm.setTerm(1, normalizedLangTag);
					}
				}
			}
		}

		// Normalizing equalities

		for (CQIE cq: unfoldingProgram)
			EQNormalizer.enforceEqualities(cq);
	}

	/***
	 * Adding ontology assertions (ABox) as rules (facts, head with no body).
	 */
	private void addAssertionsAsFacts(List<CQIE> unfoldingProgram, Iterable<ClassAssertion> cas,
									  Iterable<ObjectPropertyAssertion> pas, Iterable<DataPropertyAssertion> das) {

		int count = 0;
		for (ClassAssertion ca : cas) {
			// no blank nodes are supported here
			URIConstant c = (URIConstant) ca.getIndividual();
			Predicate p = ca.getConcept().getPredicate();
			Function head = fac.getFunction(p,
					uriTemplateMatcher.generateURIFunction(c.getURI()));
			CQIE rule = fac.getCQIE(head, Collections.<Function> emptyList());

			unfoldingProgram.add(rule);
			count++;
		}
		log.debug("Appended {} class assertions from ontology as fact rules", count);

		count = 0;
		for (ObjectPropertyAssertion pa : pas) {
			// no blank nodes are supported here
			URIConstant s = (URIConstant)pa.getSubject();
			URIConstant o = (URIConstant)pa.getObject();
			Predicate p = pa.getProperty().getPredicate();
			Function head = fac.getFunction(p,
							uriTemplateMatcher.generateURIFunction(s.getURI()),
							uriTemplateMatcher.generateURIFunction(o.getURI()));
			CQIE rule = fac.getCQIE(head, Collections.<Function> emptyList());

			unfoldingProgram.add(rule);
			count++;
		}
		log.debug("Appended {} object property assertions as fact rules", count);


//		int count = 0;
//		for (DataPropertyAssertion a : assertions) {
		// WE IGNORE DATA PROPERTY ASSERTIONS UNTIL THE NEXT RELEASE
//			DataPropertyAssertion ca = (DataPropertyAssertion) assertion;
//			ObjectConstant s = ca.getObject();
//			ValueConstant o = ca.getValue();
//			String typeURI = getURIType(o.getType());
//			Predicate p = ca.getPredicate();
//			Predicate urifuction = factory.getUriTemplatePredicate(1);
//			head = factory.getFunction(p, factory.getFunction(urifuction, s), factory.getFunction(factory.getPredicate(typeURI,1), o));
//			rule = factory.getCQIE(head, new LinkedList<Function>());
//		}

//		}
//		log.debug("Appended {} ABox assertions as fact rules", count);
	}

	/***
	 * Adding data typing on the mapping axioms.
	 */

	public void extendTypesWithMetadata(List<CQIE> unfoldingProgram, TBoxReasoner tBoxReasoner,
										VocabularyValidator vocabularyValidator, DataSourceMetadata metadata) throws OBDAException {
		if (metadata instanceof DBMetadata) {
			MappingDataTypeRepair typeRepair = new MappingDataTypeRepair((DBMetadata)metadata, tBoxReasoner,
					vocabularyValidator);

			for (CQIE rule : unfoldingProgram) {
				typeRepair.insertDataTyping(rule);
			}
		}
		/**
		 * TODO: refactor so as to support this case
		 */
		else {
			log.warn("data-type reparation not supported for not SQL DBMetadata");
		}
	}

	/***
	 * Adding NOT NULL conditions to the variables used in the head
	 * of all mappings to preserve SQL-RDF semantics
	 * @param unfoldingProgram
	 */

	public void addNOTNULLToMappings(List<CQIE> unfoldingProgram) {

		for (CQIE mapping : unfoldingProgram) {
			Set<Variable> headvars = new HashSet<>();
			TermUtils.addReferencedVariablesTo(headvars, mapping.getHead());
			for (Variable var : headvars) {
				List<Function> body = mapping.getBody();
				if (isNullable(var, body)) {
					Function notnull = fac.getFunctionIsNotNull(var);
					if (!body.contains(notnull))
						body.add(notnull);
				}
			}
		}
	}

	/**
	 * Returns false if it detects that the variable is guaranteed not being null.
	 */
	private boolean isNullable(Variable variable, List<Function> bodyAtoms) {
		/**
		 * NB: only looks for data atoms in a flat mapping (no algebraic (meta-)predicate such as LJ).
		 */
		ImmutableList<Function> definingAtoms = bodyAtoms.stream()
				.filter(Function::isDataFunction)
				.filter(a -> a.containsTerm(variable))
				.collect(ImmutableCollectors.toList());

		switch(definingAtoms.size()) {
			case 0:
				// May happen if a meta-predicate is used
				return true;
			case 1:
				break;
			/**
			 * Implicit joining conditions so not nullable.
			 *
			 * Rare.
			 */
			default:
				return false;
		}

		Function definingAtom = definingAtoms.get(0);

		/**
		 * Look for non-null
		 */
		if (hasNonNullColumnForVariable(definingAtom, variable))
			return false;

		/**
		 * TODO: check filtering conditions
		 */

		/**
		 * Implicit equality inside the data atom.
		 *
		 * Rare.
		 */
		if (definingAtom.getTerms().stream()
				.filter(t -> t.equals(variable))
				.count() > 1) {
			return false;
		}

		/**
		 * No constraint found --> may be null
		 */
		return true;
	}


	private static ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> convertUniqueConstraints(
			Multimap<Predicate, List<Integer>> pkeys) {
		Map<Predicate, AtomPredicate> predicateMap = new HashMap<>();

		ImmutableMultimap.Builder<AtomPredicate, ImmutableList<Integer>> multimapBuilder = ImmutableMultimap.builder();

		for(Map.Entry<Predicate, List<Integer>> entry : pkeys.entries()) {
			Predicate originalPredicate = entry.getKey();
			AtomPredicate atomPredicate;
			if (originalPredicate instanceof AtomPredicate) {
				atomPredicate = (AtomPredicate) originalPredicate;
			}
			else if (predicateMap.containsKey(originalPredicate)) {
				atomPredicate = predicateMap.get(originalPredicate);
			}
			else {
				atomPredicate = new AtomPredicateImpl(originalPredicate);
				predicateMap.put(originalPredicate, atomPredicate);
			}

			multimapBuilder.put(atomPredicate, ImmutableList.copyOf(entry.getValue()));
		}
		return multimapBuilder.build();
	}

	public List<CQIE> applyTMappings(List<CQIE> unfoldingProgram, TBoxReasoner reformulationReasoner, boolean full, DataSourceMetadata metadata,
							   DBConnector dbConnector, TMappingExclusionConfig excludeFromTMappings) throws OBDAException {

		final long startTime = System.currentTimeMillis();

		// for eliminating redundancy from the unfolding program
		LinearInclusionDependencies foreignKeyRules = dbConnector.generateFKRules(metadata);
		CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules);
		// Davide> Here now I put another TMappingProcessor taking
		//         also a list of Predicates as input, that represents
		//         what needs to be excluded from the T-Mappings
		//if( applyExcludeFromTMappings )
			unfoldingProgram = TMappingProcessor.getTMappings(unfoldingProgram, reformulationReasoner, full,
					foreignKeyCQC, excludeFromTMappings);
		//else
		//	unfoldingProgram = TMappingProcessor.getTMappings(unfoldingProgram, reformulationReasoner, full);

		// Eliminating redundancy from the unfolding program
		// TODO: move the foreign-key optimisation inside t-mapping generation
		//              -- at this point it has little effect

/*
		int s0 = unfoldingProgram.size();
		Collections.sort(unfoldingProgram, CQCUtilities.ComparatorCQIE);
		CQCUtilities.removeContainedQueries(unfoldingProgram, foreignKeyCQC);
		if (s0 != unfoldingProgram.size())
			System.err.println("CQC REMOVED: " + s0 + " - " + unfoldingProgram.size());
*/

		final long endTime = System.currentTimeMillis();
		log.debug("TMapping size: {}", unfoldingProgram.size());
		log.debug("TMapping processing time: {} ms", (endTime - startTime));

		return unfoldingProgram;
	}

	
	/***
	 * Creates mappings with heads as "triple(x,y,z)" from mappings with binary
	 * and unary atoms"
	 *
	 * @return
	 */
	private static List<CQIE> generateTripleMappings(List<CQIE> unfoldingProgram) {
		List<CQIE> newmappings = new LinkedList<CQIE>();

		for (CQIE mapping : unfoldingProgram) {
			Function newhead = null;
			Function currenthead = mapping.getHead();
			if (currenthead.getArity() == 1) {
				/*
				 * head is Class(x) Forming head as triple(x,uri(rdf:type),
				 * uri(Class))
				 */
				Function rdfTypeConstant = fac.getUriTemplate(fac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));

				String classname = currenthead.getFunctionSymbol().getName();
				Term classConstant = fac.getUriTemplate(fac.getConstantLiteral(classname));

				newhead = fac.getTripleAtom(currenthead.getTerm(0), rdfTypeConstant, classConstant);
			} 
			else if (currenthead.getArity() == 2) {
				/*
				 * head is Property(x,y) Forming head as triple(x,uri(Property),
				 * y)
				 */
				String propname = currenthead.getFunctionSymbol().getName();
				Function propConstant = fac.getUriTemplate(fac.getConstantLiteral(propname));

				newhead = fac.getTripleAtom(currenthead.getTerm(0), propConstant, currenthead.getTerm(1));
			}
			else {
				/*
				 * head is triple(x,uri(Property),y)
				 */
				newhead = (Function) currenthead.clone();
			}
			CQIE newmapping = fac.getCQIE(newhead, mapping.getBody());
			newmappings.add(newmapping);
		}
		return newmappings;
	}

	/**
	 * Store information about owl:sameAs
	 */
	public void addSameAsMapping(List<CQIE> unfoldingProgram) throws OBDAException{


		MappingSameAs msa = new MappingSameAs(unfoldingProgram);

		dataPropertiesAndClassesMapped = msa.getDataPropertiesAndClassesWithSameAs();
		objectPropertiesMapped =  msa.getObjectPropertiesWithSameAs();


	}

	public Set<Predicate> getSameAsDataPredicatesAndClasses(){

		return dataPropertiesAndClassesMapped;
	}

	public Set<Predicate> getSameAsObjectPredicates(){

		return objectPropertiesMapped;
	}

	public UriTemplateMatcher getUriTemplateMatcher() {
		return uriTemplateMatcher;
	}

	public ImmutableMultimap<Predicate, CQIE> getMappings(){
		return unfolder.getMappings();
	}

	public ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> getUniqueConstraints() {
		return uniqueConstraints;
	}

	public DatalogUnfolder getDatalogUnfolder() {
		return unfolder;
	}

	public MetadataForQueryOptimization getMetadataForQueryOptimization() {
		return metadataForQueryOptimization;
	}
}
