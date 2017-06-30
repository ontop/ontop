package it.unibz.inf.ontop.mapping.conversion.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.mapping.transf.MappingNormalizer;
import it.unibz.inf.ontop.mapping.conversion.SQLPPMapping2OBDASpecificationConverter;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.mapping.pp.validation.PPMappingOntologyComplianceValidator;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.impl.TermUtils;
import it.unibz.inf.ontop.nativeql.RDBMetadataExtractor;
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.ontology.utils.MappingVocabularyExtractor;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.*;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.*;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;
import it.unibz.inf.ontop.sql.RDBMetadata;
import it.unibz.inf.ontop.sql.Relation2DatalogPredicate;
import it.unibz.inf.ontop.sql.RelationID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.SQLPPMapping2DatalogConverter;
import it.unibz.inf.ontop.utils.MetaMappingExpander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DefaultSQLPPMapping2OBDASpecificationConverter implements SQLPPMapping2OBDASpecificationConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSQLPPMapping2OBDASpecificationConverter.class);

    private final OntopMappingSQLSettings settings;
    private final RDBMetadataExtractor dbMetadataExtractor;
    private final TMappingExclusionConfig tMappingExclusionConfig;
    private final Datalog2QueryMappingConverter mappingConverter;
    private final SpecificationFactory specificationFactory;
    private final MappingNormalizer mappingNormalizer;
    private final PPMappingOntologyComplianceValidator ontologyComplianceValidator;

    @Inject
    private DefaultSQLPPMapping2OBDASpecificationConverter(OntopMappingSQLSettings settings,
                                                           NativeQueryLanguageComponentFactory nativeQLFactory,
                                                           TMappingExclusionConfig tMappingExclusionConfig,
                                                           Datalog2QueryMappingConverter mappingConverter,
                                                           SpecificationFactory specificationFactory,
                                                           MappingNormalizer mappingNormalizer,
                                                           PPMappingOntologyComplianceValidator ontologyComplianceValidator) {
        this.settings = settings;
        this.dbMetadataExtractor = nativeQLFactory.create();
        this.tMappingExclusionConfig = tMappingExclusionConfig;
        this.mappingConverter = mappingConverter;
        this.specificationFactory = specificationFactory;
        this.mappingNormalizer = mappingNormalizer;
        this.ontologyComplianceValidator = ontologyComplianceValidator;
    }

    @Override
    public OBDASpecification convert(final SQLPPMapping initialPPMapping, Optional<DBMetadata> optionalDBMetadata,
                                     Optional<Ontology> optionalOntology, Optional<File> constraintFile,
                                     ExecutorRegistry executorRegistry)
            throws DBMetadataExtractionException, MappingException {


        optionalOntology.ifPresent(o -> ontologyComplianceValidator.validateMapping(
                initialPPMapping,
                o.getVocabulary(),
                TBoxReasonerImpl.create(
                        o,
                        settings.isEquivalenceOptimizationEnabled()
                )));

        RDBMetadata dbMetadata = extractDBMetadata(initialPPMapping, optionalDBMetadata, constraintFile);

        ImmutableList<SQLPPTriplesMap> expandedMappingAxioms = MetaMappingExpander.expand(
                initialPPMapping.getTripleMaps(), settings, dbMetadata);

        // NB: may also add views in the DBMetadata (for non-understood SQL queries)
        ImmutableList<CQIE> initialMappingRules = convertPPTriplesMaps(expandedMappingAxioms, dbMetadata);
        dbMetadata.freeze();

        /*
         * Transformations at the Datalog level
         */
        return transformMapping(initialMappingRules, dbMetadata, optionalOntology, initialPPMapping.getMetadata(),
                executorRegistry);
    }

    /**
     * Makes use of the DB connection
     */
    private RDBMetadata extractDBMetadata(final SQLPPMapping ppMapping, Optional<DBMetadata> optionalDBMetadata,
                                          Optional<File> constraintFile)
            throws DBMetadataExtractionException, MetaMappingExpansionException {

        try (Connection localConnection = createConnection()) {
            return optionalDBMetadata.isPresent()
                    ? dbMetadataExtractor.extract(ppMapping, localConnection, optionalDBMetadata.get(), constraintFile)
                    : dbMetadataExtractor.extract(ppMapping, localConnection, constraintFile);
        }
        /*
         * Problem while creating the connection
         */
        catch (SQLException e) {
            throw new DBMetadataExtractionException(e.getMessage());
        }
    }

    /**
     * May also add views in the DBMetadata!
     */
    private ImmutableList<CQIE> convertPPTriplesMaps(ImmutableList<SQLPPTriplesMap> mappingAxioms, RDBMetadata dbMetadata) {


        ImmutableList<CQIE> unfoldingProgram = SQLPPMapping2DatalogConverter.constructDatalogProgram(mappingAxioms, dbMetadata);

        LOGGER.debug("Original mapping size: {}", unfoldingProgram.size());

        // TODO: move it to the converter
        // Normalizing language tags and equalities
        normalizeMapping(unfoldingProgram);

        return unfoldingProgram;
    }

    private OBDASpecification transformMapping(ImmutableList<CQIE> initialMappingRules,
                                               RDBMetadata dbMetadata, Optional<Ontology> optionalOntology,
                                               MappingMetadata mappingMetadata,
                                               ExecutorRegistry executorRegistry) throws MappingException {

        Ontology ontology = optionalOntology
                //  extract ontology from the mapping if it does not exist
                .orElseGet(() -> MappingVocabularyExtractor.extractOntology(initialMappingRules.stream()
                        .map(CQIE::getHead)));

        TBoxReasoner tBox = TBoxReasonerImpl.create(ontology, settings.isEquivalenceOptimizationEnabled());

        ImmutableOntologyVocabulary vocabulary = ontology.getVocabulary();

        // Adding data typing on the mapping axioms.
        ImmutableList<CQIE> fullyTypedRules = inferMissingDataTypes(initialMappingRules, dbMetadata);

        ImmutableList<CQIE> mappingRulesWithFacts = insertFacts(fullyTypedRules, ontology, mappingMetadata.getUriTemplateMatcher());

        ImmutableList<CQIE> saturatedMappingRules = saturateMapping(mappingRulesWithFacts, tBox, vocabulary, dbMetadata);

        Mapping saturatedMapping = mappingConverter.convertMappingRules(saturatedMappingRules, dbMetadata,
                executorRegistry, mappingMetadata);

        Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        return specificationFactory.createSpecification(normalizedMapping, dbMetadata, tBox, vocabulary);
    }


    private ImmutableList<CQIE> insertFacts(ImmutableList<CQIE> mapping, Ontology ontology,
                                            UriTemplateMatcher uriTemplateMatcher) {
        // Adding ontology assertions (ABox) as rules (facts, head with no body).
        List<AnnotationAssertion> annotationAssertions;
        if (settings.isOntologyAnnotationQueryingEnabled()) {
            annotationAssertions = ontology.getAnnotationAssertions();
        }
        else{
            annotationAssertions = Collections.emptyList();
        }

        // Adding ontology assertions (ABox) as rules (facts, head with no body).
        return addAssertionsAsFacts(mapping, ontology.getClassAssertions(),
                ontology.getObjectPropertyAssertions(), ontology.getDataPropertyAssertions(), annotationAssertions,
                uriTemplateMatcher);
    }


    private ImmutableList<CQIE> saturateMapping(ImmutableList<CQIE> mapping, TBoxReasoner tBox,
                                                ImmutableOntologyVocabulary vocabulary,
                                                RDBMetadata dbMetadata) {

        ImmutableList<CQIE> equivalenceFreeMappingRules = replaceEquivalences(mapping, tBox, vocabulary);

        ImmutableList<CQIE> sameAsEnrichedRules = settings.isSameAsInMappingsEnabled()
                ? MappingSameAs.addSameAsInverse(equivalenceFreeMappingRules)
                : equivalenceFreeMappingRules;

        List<CQIE> canonicalMapping = new ArrayList<>(sameAsEnrichedRules);
        canonicalMapping = new CanonicalIRIRewriter().buildCanonicalIRIMappings(canonicalMapping);

        /*
         * Adding NOT NULL conditions to the variables used in the head
         * of all mappings to preserve SQL-RDF semantics
         *
         * Historical note: it was (wrongly) done BEFORE the saturation
         */
        addNOTNULLToMappings(canonicalMapping, dbMetadata);

        // Apply TMappings
        List<CQIE> saturatedMapping = applyTMappings(canonicalMapping, tBox, true, dbMetadata);

        // A second time is needed before the optimization applied after mapping saturation is NOT NULL-AWARE...
        addNOTNULLToMappings(saturatedMapping, dbMetadata);

        if(LOGGER.isDebugEnabled()) {
            String finalMappings = Joiner.on("\n").join(saturatedMapping);
            LOGGER.debug("Set of mappings after saturation: \n {}", finalMappings);
        }

        // Adding "triple(x,y,z)" mappings for support of unbounded
        // predicates and variables as class names (implemented in the
        // sparql translator)
        saturatedMapping.addAll(generateTripleMappings(saturatedMapping));

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Final set of mappings: \n {}", Joiner.on("\n").join(saturatedMapping));

        return ImmutableList.copyOf(saturatedMapping);
    }

    private ImmutableList<CQIE> replaceEquivalences(ImmutableList<CQIE> mappingRules,
                                                    TBoxReasoner tBox, ImmutableOntologyVocabulary vocabulary) {
        if (settings.isEquivalenceOptimizationEnabled()) {
            MappingVocabularyValidator vocabularyValidator = new MappingVocabularyValidator(tBox, vocabulary);
            return vocabularyValidator.replaceEquivalences(mappingRules);
        }
        else
            return mappingRules;
    }

    /**
     * Normalize language tags (make them lower-case) and equalities
     * (remove them by replacing all equivalent terms with one representative)
     */

    private void normalizeMapping(List<CQIE> unfoldingProgram) {

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
                        Term normalizedLangTag = DATA_FACTORY.getConstantLiteral(originalLangConstant.getValue().toLowerCase(),
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
    private ImmutableList<CQIE> addAssertionsAsFacts(ImmutableList<CQIE> mapping, Iterable<ClassAssertion> cas,
                                                     Iterable<ObjectPropertyAssertion> pas,
                                                     Iterable<DataPropertyAssertion> das, List<AnnotationAssertion> aas,
                                                     UriTemplateMatcher uriTemplateMatcher) {

        List<CQIE> mutableMapping = new ArrayList<>(mapping);

        int count = 0;
        for (ClassAssertion ca : cas) {
            // no blank nodes are supported here
            URIConstant c = (URIConstant) ca.getIndividual();
            Predicate p = ca.getConcept().getPredicate();
            Function head = DATA_FACTORY.getFunction(p,
                    uriTemplateMatcher.generateURIFunction(c.getURI()));
            CQIE rule = DATA_FACTORY.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", count);

        count = 0;
        for (ObjectPropertyAssertion pa : pas) {
            // no blank nodes are supported here
            URIConstant s = (URIConstant)pa.getSubject();
            URIConstant o = (URIConstant)pa.getObject();
            Predicate p = pa.getProperty().getPredicate();
            Function head = DATA_FACTORY.getFunction(p,
                    uriTemplateMatcher.generateURIFunction(s.getURI()),
                    uriTemplateMatcher.generateURIFunction(o.getURI()));
            CQIE rule = DATA_FACTORY.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }
        LOGGER.debug("Appended {} object property assertions as fact rules", count);


        count = 0;
        for (DataPropertyAssertion da : das) {
            // no blank nodes are supported here
            URIConstant s = (URIConstant)da.getSubject();
            ValueConstant o = da.getValue();
            Predicate p = da.getProperty().getPredicate();

            Function head;
            if(o.getLanguage()!=null){
                head = DATA_FACTORY.getFunction(p, DATA_FACTORY.getUriTemplate(
                        DATA_FACTORY.getConstantLiteral(s.getURI())),
                        DATA_FACTORY.getTypedTerm(DATA_FACTORY.getConstantLiteral(o.getValue()),o.getLanguage()));
            }
            else {

                head = DATA_FACTORY.getFunction(p, DATA_FACTORY.getUriTemplate(
                        DATA_FACTORY.getConstantLiteral(s.getURI())), DATA_FACTORY.getTypedTerm(o, o.getType()));
            }
            CQIE rule = DATA_FACTORY.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count ++;
        }

        LOGGER.debug("Appended {} data property assertions as fact rules", count);

        count = 0;
        for (AnnotationAssertion aa : aas) {
            // no blank nodes are supported here

            URIConstant s = (URIConstant) aa.getSubject();
            Constant v = aa.getValue();
            Predicate p = aa.getProperty().getPredicate();

            Function head;
            if (v instanceof ValueConstant) {

                ValueConstant o = (ValueConstant) v;

                if (o.getLanguage() != null) {
                    head = DATA_FACTORY.getFunction(p, DATA_FACTORY.getUriTemplate(
                            DATA_FACTORY.getConstantLiteral(s.getURI())),
                            DATA_FACTORY.getTypedTerm(DATA_FACTORY.getConstantLiteral(o.getValue()), o.getLanguage()));
                } else {

                    head = DATA_FACTORY.getFunction(p, DATA_FACTORY.getUriTemplate(
                            DATA_FACTORY.getConstantLiteral(s.getURI())), DATA_FACTORY.getTypedTerm(o, o.getType()));
                }
            } else {

                URIConstant o = (URIConstant) v;
                head = DATA_FACTORY.getFunction(p,
                        DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(s.getURI())),
                        DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(o.getURI())));


            }
            CQIE rule = DATA_FACTORY.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }

        LOGGER.debug("Appended {} annotation assertions as fact rules", count);
        return ImmutableList.copyOf(mutableMapping);
    }

    /***
     * Infers missing data types.
     */
    public ImmutableList<CQIE> inferMissingDataTypes(ImmutableList<CQIE> unfoldingProgram, DBMetadata metadata)
            throws MappingException {

        MappingDataTypeCompletion typeCompletion = new MappingDataTypeCompletion(metadata);
        for (CQIE rule : unfoldingProgram) {
            typeCompletion.insertDataTyping(rule);
        }

        return unfoldingProgram;
    }

    /***
     * Adding NOT NULL conditions to the variables used in the head
     * of all mappings to preserve SQL-RDF semantics
     *
     */
    private static void addNOTNULLToMappings(List<CQIE> unfoldingProgram, DBMetadata metadata) {

        for (CQIE mapping : unfoldingProgram) {
            Set<Variable> headvars = new HashSet<>();
            TermUtils.addReferencedVariablesTo(headvars, mapping.getHead());
            for (Variable var : headvars) {
                List<Function> body = mapping.getBody();
                if (isNullable(var, body, metadata)) {
                    Function notnull = DATA_FACTORY.getFunctionIsNotNull(var);
                    if (!body.contains(notnull))
                        body.add(notnull);
                }
            }
        }
    }


    private List<CQIE> applyTMappings(List<CQIE> unfoldingProgram, TBoxReasoner reformulationReasoner, boolean full,
                                      DBMetadata metadata) {

        final long startTime = System.currentTimeMillis();

        // for eliminating redundancy from the unfolding program
        LinearInclusionDependencies foreignKeyRules = new LinearInclusionDependencies(metadata.generateFKRules());
        CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules);
        // Davide> Here now I put another TMappingProcessor taking
        //         also a list of Predicates as input, that represents
        //         what needs to be excluded from the T-Mappings
        //if( applyExcludeFromTMappings )
        unfoldingProgram = TMappingProcessor.getTMappings(unfoldingProgram, reformulationReasoner, full,
                foreignKeyCQC, tMappingExclusionConfig);
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
        LOGGER.debug("TMapping size: {}", unfoldingProgram.size());
        LOGGER.debug("TMapping processing time: {} ms", (endTime - startTime));

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
                Function rdfTypeConstant = DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(OBDAVocabulary.RDF_TYPE));

                String classname = currenthead.getFunctionSymbol().getName();
                Term classConstant = DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(classname));

                newhead = DATA_FACTORY.getTripleAtom(currenthead.getTerm(0), rdfTypeConstant, classConstant);
            }
            else if (currenthead.getArity() == 2) {
				/*
				 * head is Property(x,y) Forming head as triple(x,uri(Property),
				 * y)
				 */
                String propname = currenthead.getFunctionSymbol().getName();
                Function propConstant = DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(propname));

                newhead = DATA_FACTORY.getTripleAtom(currenthead.getTerm(0), propConstant, currenthead.getTerm(1));
            }
            else {
				/*
				 * head is triple(x,uri(Property),y)
				 */
                newhead = (Function) currenthead.clone();
            }
            CQIE newmapping = DATA_FACTORY.getCQIE(newhead, mapping.getBody());
            newmappings.add(newmapping);
        }
        return newmappings;
    }

    /**
     * Returns false if it detects that the variable is guaranteed not being null.
     */
    private static boolean isNullable(Variable variable, List<Function> bodyAtoms, DBMetadata metadata) {
        /*
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
            /*
             * Implicit joining conditions so not nullable.
             *
             * Rare.
             */
            default:
                return false;
        }

        Function definingAtom = definingAtoms.get(0);

        /*
         * Look for non-null
         */
        if (hasNonNullColumnForVariable(definingAtom, variable, metadata))
            return false;

        /*
         * TODO: check filtering conditions
         */

        /*
         * Implicit equality inside the data atom.
         *
         * Rare.
         */
        if (definingAtom.getTerms().stream()
                .filter(t -> t.equals(variable))
                .count() > 1) {
            return false;
        }

        /*
         * No constraint found --> may be null
         */
        return true;
    }

    private static boolean hasNonNullColumnForVariable(Function atom, Variable variable, DBMetadata metadata) {
        RelationID relationId = Relation2DatalogPredicate.createRelationFromPredicateName(metadata.getQuotedIDFactory(),
                atom.getFunctionSymbol());
        DatabaseRelationDefinition relation = metadata.getDatabaseRelation(relationId);

        if (relation == null)
            return false;

        List<Term> arguments = atom.getTerms();

        // NB: DB column indexes start at 1.
        return IntStream.range(1, arguments.size() + 1)
                .filter(i -> arguments.get(i - 1).equals(variable))
                .mapToObj(relation::getAttribute)
                .anyMatch(att -> !att.canNull());
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(settings.getJdbcUrl(),
                settings.getJdbcUser(), settings.getJdbcPassword());
    }
}
