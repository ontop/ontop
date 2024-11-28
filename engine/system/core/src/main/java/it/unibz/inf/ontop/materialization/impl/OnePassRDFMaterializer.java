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
import it.unibz.inf.ontop.evaluator.QueryContext;
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
    private final QueryContext.Factory queryContextFactory;
    private final TermFactory termFactory;
    private final QueryTransformerFactory queryTransformerFactory;

    private final ImmutableMap<IRI, VocabularyEntry> vocabulary;
    private final ImmutableList<MappingAssertionInformation> mappingInformation;

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
        this.queryContextFactory = injector.getInstance(QueryContext.Factory.class);
        this.termFactory = injector.getInstance(TermFactory.class);
        this.queryTransformerFactory = injector.getInstance(QueryTransformerFactory.class);

        Mapping saturatedMapping = specification.getSaturatedMapping();
        ImmutableList<IQ> mappingAssertionsIQs = saturatedMapping.getRDFAtomPredicates().stream()
                .map(saturatedMapping::getQueries)
                .flatMap(Collection::stream)
                .map(iq -> removeDistincts(iq, materializationParams.areDuplicatesAllowed()))
                .map(this::splitPotentialUnionNode)
                .flatMap(Collection::stream)
                .map(this::splitPotentialUnionNode)
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toList());

        ImmutableList<MappingAssertionInformation> tmpMappingInfo = mappingAssertionsIQs.stream()
                .map(this::createMappingAssertionInfo)
                .collect(ImmutableCollectors.toList());

        mappingInformation = mergeMappingInformation(tmpMappingInfo);
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
                queryLoggerFactory,
                queryContextFactory);
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


        ImmutableList<ImmutableList<MappingAssertionInformation>> groupedByJoinRelationsInfos = mappingInformation.stream()
                .filter(m -> m instanceof JoinMappingAssertionInfo)
                .map(m -> Map.entry(
                        m.getRelationsDefinitions().stream()
                                .map(rel -> rel.getAtomPredicate().getName())
                                .collect(ImmutableCollectors.toSet()),
                        m))
                .collect(ImmutableCollectors.toMultimap())
                .asMap().values().stream()
                .map(ImmutableList::copyOf)
                .collect(ImmutableCollectors.toList());

        ImmutableList<ImmutableList<MappingAssertionInformation>> groupedBySingleRelationInfo = mappingInformation.stream()
                .filter(m -> !(m instanceof ComplexMappingAssertionInfo || m instanceof JoinMappingAssertionInfo))
                .map(m -> Map.entry(
                        m.getRelationsDefinitions().get(0).getAtomPredicate().getName(),
                        m))
                .collect(ImmutableCollectors.toMultimap())
                .asMap().values().stream()
                .map(ImmutableList::copyOf)
                .collect(ImmutableCollectors.toList());

        return ImmutableList.<MappingAssertionInformation>builder()
                .addAll(mergeCompatibleAssertionsInfos(groupedByJoinRelationsInfos))
                .addAll(mergeCompatibleAssertionsInfos(groupedBySingleRelationInfo))
                .addAll(complexMappingAssertionInfo)
                .build();
    }

    private MappingAssertionInformation createMappingAssertionInfo(IQ mappingAssertionIQ) {
        IQTree tree = mappingAssertionIQ.getTree();
        RDFFactTemplates rdfTemplates = new RDFFactTemplatesImpl(ImmutableList.of((mappingAssertionIQ.getProjectionAtom().getArguments())));

        if (hasNotSupportedNode(tree)) {
            return new ComplexMappingAssertionInfo(tree, rdfTemplates, substitutionFactory);
        }

        Optional<IQTree> joinSubtree = findJoinSubtree(tree);
        if (joinSubtree.isPresent()) {
            return new JoinMappingAssertionInfo(
                    tree,
                    rdfTemplates,
                    joinSubtree.get(),
                    mappingAssertionIQ.getVariableGenerator(),
                    iqFactory,
                    substitutionFactory);
        }


        ImmutableList<ExtensionalDataNode> extensionalNodes = findExtensionalNodes(tree);
        if (extensionalNodes.size() != 1) {
            return new ComplexMappingAssertionInfo(tree, rdfTemplates, substitutionFactory);
        }
        ExtensionalDataNode extensionalNode = extensionalNodes.get(0);
        RelationDefinition relation = extensionalNode.getRelationDefinition();

        if (hasFilterNode(tree)) {
            return  new FilterMappingAssertionInfo(
                            tree,
                            rdfTemplates,
                            extensionalNode,
                            mappingAssertionIQ.getVariableGenerator(),
                            iqFactory,
                            termFactory,
                            substitutionFactory);
        }

        ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap = extensionalNode.getArgumentMap();
        if (argumentMap.values().stream().allMatch(v -> v instanceof Variable)) {
            return new SimpleMappingAssertionInfo(relation,
                    (ImmutableMap<Integer, Variable>) argumentMap,
                    tree,
                    rdfTemplates,
                    mappingAssertionIQ.getVariableGenerator(),
                    iqFactory,
                    substitutionFactory);
        } else if (argumentMap.values().stream().anyMatch(v -> v instanceof DBConstant)) {
            ImmutableMap<Integer, Attribute> constantAttributes = argumentMap.entrySet().stream()
                    .filter(e -> e.getValue() instanceof DBConstant)
                    .map(Map.Entry::getKey)
                    .collect(ImmutableCollectors.toMap(
                            k -> k,
                            k -> relation.getAttribute(k + 1)));

            //return new ComplexMappingAssertionInfo(tree, rdfTemplates, substitutionFactory);
            return new DictionaryPatternMappingAssertion(tree,
                    rdfTemplates,
                    constantAttributes,
                    extensionalNode,
                    mappingAssertionIQ.getVariableGenerator(),
                    iqFactory,
                    substitutionFactory,
                    termFactory,
                    queryTransformerFactory);

        } else {
            return new ComplexMappingAssertionInfo(mappingAssertionIQ.getTree(), rdfTemplates, substitutionFactory);
        }
    }

    private ImmutableList<IQ> splitPotentialUnionNode(IQ iq) {
        IQTree tree = iq.getTree();
        if (!(tree.getRootNode() instanceof ConstructionNode)) {
            throw new MinorOntopInternalBugException("The root node of a mapping is expected to be a ConstructionNode");
        }
        if (tree.getChildren().get(0).getRootNode() instanceof UnionNode) {
            ImmutableList<IQTree> unionChildren = tree.getChildren().get(0).getChildren();
            return unionChildren.stream()
                    .map(unionChild -> {
                        IQTree newTree = iqFactory.createUnaryIQTree(
                                (ConstructionNode) tree.getRootNode(), unionChild);
                        return iqFactory.createIQ(iq.getProjectionAtom(),
                                newTree.normalizeForOptimization(iq.getVariableGenerator()));
                    })
                    .collect(ImmutableCollectors.toList());
        } else {
            return ImmutableList.of(iq);
        }
    }

    private ImmutableList<MappingAssertionInformation> mergeCompatibleAssertionsInfos(
            ImmutableList<ImmutableList<MappingAssertionInformation>> compatibleAssertionInfos) {
        ArrayList<MappingAssertionInformation> unmergedMappingAssertionInfos = new ArrayList<>();
        ImmutableList<MappingAssertionInformation> mergedMappingAssertionInfos = compatibleAssertionInfos.stream()
                .map( sameRelationMappings -> {
                    MappingAssertionInformation mergedSameRelationMapping = sameRelationMappings.get(0);
                    for (int i=1; i<sameRelationMappings.size(); i++) {
                        var m1 = sameRelationMappings.get(i);
                        Optional<MappingAssertionInformation> merged = mergedSameRelationMapping.merge(m1);
                        if (merged.isPresent()) {
                            mergedSameRelationMapping = merged.get();
                        } else {
                            unmergedMappingAssertionInfos.add(m1);
                        }
                    }
                    return mergedSameRelationMapping;
                })
                .collect(ImmutableCollectors.toList());

        return ImmutableList.<MappingAssertionInformation>builder()
                .addAll(mergedMappingAssertionInfos)
                .addAll(unmergedMappingAssertionInfos)
                .build();
    }

    private IQ removeDistincts(IQ iq, boolean allowDuplicates) {
        return allowDuplicates
            ? iqFactory.createIQ(iq.getProjectionAtom(), iq.getTree().removeDistincts())
            : iq;
    }

    /**
     * Recursive
     */
    private Optional<IQTree> findJoinSubtree(IQTree tree) {
        if (tree.getRootNode() instanceof JoinLikeNode) {
            return Optional.of(tree);
        } else {
            return tree.getChildren().stream()
                    .map(this::findJoinSubtree)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findAny();
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
    private boolean hasFilterNode(IQTree tree) {
        if (tree.getRootNode() instanceof FilterNode) {
            return true;
        } else {
            return tree.getChildren().stream()
                    .anyMatch(this::hasFilterNode);
        }
    }

    /**
     * Recursive
     */
    private boolean hasNotSupportedNode(IQTree tree) {
        if (tree.getRootNode() instanceof QueryModifierNode
                || tree.getRootNode() instanceof UnionNode
                || tree.getRootNode() instanceof AggregationNode
                || tree.getRootNode() instanceof LeftJoinNode){
            return true;
        } else {
            return tree.getChildren().stream()
                    .anyMatch(this::hasNotSupportedNode);
        }
    }
}
