package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.materialization.MappingAssertionInformation;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.QuadPredicate;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OnePassRDFMaterializer implements OntopRDFMaterializer {

    private final MaterializationParams params;
    private final OntopQueryEngine queryEngine;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final NativeQueryGenerator nativeQueryGenerator;
    private final AtomFactory atomFactory;
    private final GeneralStructuralAndSemanticIQOptimizer iqOptimizer;
    private final QueryPlanner queryPlanner;
    private final QueryLogger.Factory queryLoggerFactory;
    private final TermFactory termFactory;
    private final QueryTransformerFactory queryTransformerFactory;

    private final ImmutableMap<IRI, VocabularyEntry> vocabulary;
    private final ImmutableList<MappingAssertionInformation> mappingInformation;
    private final Map<Attribute, ArrayList<IQ>> constantsDictionaryPatterns;

    public OnePassRDFMaterializer(OntopSystemConfiguration configuration, MaterializationParams materializationParams) throws OBDASpecificationException {
        Injector injector = configuration.getInjector();
        OntopSystemFactory engineFactory = injector.getInstance(OntopSystemFactory.class);

        OBDASpecification specification = configuration.loadSpecification();
        this.queryEngine = engineFactory.create(specification);
        this.vocabulary = extractVocabulary(specification.getSaturatedMapping());
        this.params = materializationParams;
        this.iqFactory = injector.getInstance(IntermediateQueryFactory.class);
        this.substitutionFactory = injector.getInstance(SubstitutionFactory.class);
        this.nativeQueryGenerator = injector.getInstance(TranslationFactory.class).create(specification.getDBParameters());
        this.atomFactory = injector.getInstance(AtomFactory.class);
        this.iqOptimizer = injector.getInstance(GeneralStructuralAndSemanticIQOptimizer.class);
        this.queryPlanner = injector.getInstance(QueryPlanner.class);
        this.queryLoggerFactory = injector.getInstance(QueryLogger.Factory.class);
        this.termFactory = injector.getInstance(TermFactory.class);
        this.queryTransformerFactory = injector.getInstance(QueryTransformerFactory.class);

        this.constantsDictionaryPatterns = new HashMap<>();

        Mapping saturatedMapping = specification.getSaturatedMapping();
        ImmutableList<IQ> mappingAssertionsIQs = saturatedMapping.getRDFAtomPredicates().stream()
                .map(saturatedMapping::getQueries)
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toList());

        ImmutableList<MappingAssertionInformation> tmpMappingInfo = mappingAssertionsIQs.stream()
                .map(this::createMappingAssertionInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toList());
        /*
        ImmutableList<MappingAssertionInformation> dictionaryPatternMappingInfo = constantsDictionaryPatterns.entrySet()
                .stream()
                .map(e -> new DictionaryPatternMappingAssertionInfo(e.getKey(),
                        e.getValue().stream().collect(ImmutableCollectors.toList()),
                        substitutionFactory, iqFactory, termFactory, queryTransformerFactory))
                .collect(ImmutableCollectors.toList());

         */
        mappingInformation = mergeMappingInformation(ImmutableList.<MappingAssertionInformation>builder()
                .addAll(tmpMappingInfo)
                //.addAll(dictionaryPatternMappingInfo)
                .build());
    }

    @Override
    public MaterializedGraphResultSet materialize() {
        return new OnePassMaterializedGraphResultSet(vocabulary,
                mappingInformation,
                params,
                queryEngine,
                nativeQueryGenerator,
                atomFactory,
                iqFactory,
                iqOptimizer,
                queryPlanner,
                queryLoggerFactory);
    }

    @Override
    public MaterializedGraphResultSet materialize(@Nonnull ImmutableSet<IRI> selectedVocabulary) {
        throw new UnsupportedOperationException("To materialize different classes/properties in separate files, use the default materializer instead.");
    }

    @Override
    public ImmutableSet<IRI> getClasses() {
        return vocabulary.entrySet().stream()
                .filter(e -> e.getValue().arity == 1)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<IRI> getProperties() {
        return vocabulary.entrySet().stream()
                .filter(e -> e.getValue().arity == 2)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());
    }

    private static ImmutableMap<IRI, VocabularyEntry> extractVocabulary(@Nonnull Mapping mapping) {
        Map<IRI, VocabularyEntry> result = new HashMap<>();
        for (RDFAtomPredicate predicate : mapping.getRDFAtomPredicates()) {
            if (predicate instanceof TriplePredicate || predicate instanceof QuadPredicate)
                result.putAll(extractTripleVocabulary(mapping, predicate)
                        .collect(ImmutableCollectors.toMap(e -> e.name, e -> e)));
        }
        return ImmutableMap.copyOf(result);
    }

    private static Stream<VocabularyEntry> extractTripleVocabulary(Mapping mapping, RDFAtomPredicate tripleOrQuadPredicate) {
        Stream<VocabularyEntry> vocabularyPropertyStream = mapping.getRDFProperties(tripleOrQuadPredicate).stream()
                .map(p -> new VocabularyEntry(p, 2));

        Stream<VocabularyEntry> vocabularyClassStream = mapping.getRDFClasses(tripleOrQuadPredicate).stream()
                .map(p -> new VocabularyEntry(p, 1));
        return Stream.concat(vocabularyClassStream, vocabularyPropertyStream);
    }

    private ImmutableList<MappingAssertionInformation> mergeMappingInformation(ImmutableList<MappingAssertionInformation> mappingInformation) {
        ImmutableList<MappingAssertionInformation> complexMappingAssertionInfo = mappingInformation.stream()
                .filter(m -> m instanceof ComplexMappingAssertionInfo)
                .collect(ImmutableCollectors.toList());
        /*
        ImmutableList<MappingAssertionInformation> patternMappingAssertionInfo = mappingInformation.stream()
                .filter(m -> m instanceof DictionaryPatternMappingAssertionInfo)
                .collect(ImmutableCollectors.toList());
        */
        ImmutableMap<String, ImmutableList<MappingAssertionInformation>> groupedByRelationMappingsInfo = mappingInformation.stream()
                .filter(m -> !(m instanceof ComplexMappingAssertionInfo || m instanceof DictionaryPatternMappingAssertionInfo))
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(mapping -> mapping.getRelationsDefinitions().get(0).getAtomPredicate().getName(), ImmutableCollectors.toList()),
                        ImmutableMap::copyOf
                ));

        ImmutableList<MappingAssertionInformation> mergedSimpleMappingsInfo = groupedByRelationMappingsInfo.values().stream()
                .map(sameRelationMappingsInfos -> {
                    MappingAssertionInformation firstMappingInfo = sameRelationMappingsInfos.get(0);
                    return sameRelationMappingsInfos.stream()
                            .skip(1)
                            .reduce(firstMappingInfo, (m1, m2) -> m1.merge(m2).orElseThrow());
                })
                .collect(ImmutableCollectors.toList());

        return ImmutableList.<MappingAssertionInformation>builder()
                .addAll(mergedSimpleMappingsInfo)
                .addAll(complexMappingAssertionInfo)
                //.addAll(patternMappingAssertionInfo)
                .build();
    }

    private Optional<MappingAssertionInformation> createMappingAssertionInfo(IQ mappingAssertionIQ) {
        IQTree tree = mappingAssertionIQ.getTree();
        RDFFactTemplates rdfTemplates = new RDFFactTemplatesImpl(ImmutableList.of((mappingAssertionIQ.getProjectionAtom().getArguments())));

        if (!(tree.getRootNode() instanceof ConstructionNode)) {
            throw new MinorOntopInternalBugException("The root node of a mapping is expected to be a ConstructionNode");
        }

        if (hasNotSupportedNode(tree)) {
            return Optional.of(new ComplexMappingAssertionInfo(tree, rdfTemplates));
        }

        ImmutableList<ExtensionalDataNode> extensionalNodes = findExtensionalNodes(tree);
        if (extensionalNodes.size() != 1) {
            return Optional.of(new ComplexMappingAssertionInfo(tree, rdfTemplates));
        }
        ExtensionalDataNode extensionalNode = extensionalNodes.get(0);
        RelationDefinition relation = extensionalNode.getRelationDefinition();

        Optional<IQTree> filterSubtree = findFilterSubtrees(tree);
        if (filterSubtree.isPresent()) {
            return filterSubtree.get().getRootNode() instanceof FilterNode
                    ? Optional.of(new FilterMappingAssertionInfo(
                            tree,
                            rdfTemplates,
                            extensionalNode,
                            filterSubtree.get(),
                            mappingAssertionIQ.getVariableGenerator(),
                            iqFactory,
                            termFactory,
                            substitutionFactory))
                    : Optional.of(new ComplexMappingAssertionInfo(tree, rdfTemplates));
        }

        ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap = extensionalNode.getArgumentMap();
        if (argumentMap.values().stream().allMatch(v -> v instanceof Variable)) {
            return Optional.of(new SimpleMappingAssertionInfo(relation,
                    (ImmutableMap<Integer, Variable>) argumentMap,
                    tree,
                    rdfTemplates,
                    mappingAssertionIQ.getVariableGenerator(),
                    iqFactory,
                    substitutionFactory));
        } else if (argumentMap.values().stream().anyMatch(v -> v instanceof DBConstant)) {
            // TODO: handle the case where there are multiple constants
            Attribute constantAttribute = argumentMap.entrySet().stream()
                    .filter(e -> e.getValue() instanceof DBConstant)
                    .map(Map.Entry::getKey)
                    .map(index -> relation.getAttribute(index + 1))
                    .findAny()
                    .orElseThrow();
            updatePatterns(constantAttribute, mappingAssertionIQ);
            //return Optional.empty();
            return Optional.of(new ComplexMappingAssertionInfo(tree, rdfTemplates));
        } else {
            return Optional.of(new ComplexMappingAssertionInfo(mappingAssertionIQ.getTree(), rdfTemplates));
        }
    }


    /**
     * Recursive
     */
    private ImmutableList<ExtensionalDataNode> findExtensionalNodes(IQTree tree) {
        if (tree.getChildren().isEmpty()) {
            if (tree.getRootNode() instanceof ExtensionalDataNode) {
                return ImmutableList.of((ExtensionalDataNode) tree.getRootNode());
            } else {
                return ImmutableList.of();
            }
        } else {
            return tree.getChildren().stream()
                    .map(this::findExtensionalNodes)
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toList());
        }
    }

    /**
     * Recursive
     */
    private Optional<IQTree> findFilterSubtrees(IQTree tree) {
        if (tree.getRootNode() instanceof JoinOrFilterNode) {
            return Optional.of(tree);
        } else {
            return tree.getChildren().stream()
                    .map(this::findFilterSubtrees)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findAny();
        }
    }

    /**
     * Recursive
     */
    private boolean hasNotSupportedNode(IQTree tree) {
        if (tree.getRootNode() instanceof UnionNode
                || tree.getRootNode() instanceof DistinctNode
                || tree.getRootNode() instanceof ValuesNode) {
            return true;
        } else {
            return tree.getChildren().stream()
                    .anyMatch(this::hasNotSupportedNode);
        }
    }

    private void updatePatterns(Attribute key, IQ value) {
        var tmp = constantsDictionaryPatterns.get(key);
        if (tmp == null) {
            constantsDictionaryPatterns.put(key, new ArrayList<>(Collections.singleton(value)));
        } else {
            tmp.add(value);
            constantsDictionaryPatterns.put(key, tmp);
        }
    }
}
