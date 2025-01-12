package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.materialization.MappingEntryCluster;
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
    private final QueryTransformerFactory queryTransformerFactory;
    private final TermFactory termFactory;

    private final ImmutableMap<IRI, VocabularyEntry> vocabulary;
    private final ImmutableList<MappingEntryCluster> mappingClusters;

    protected OnePassRDFMaterializer(OntopSystemConfiguration configuration, MaterializationParams materializationParams) throws OBDASpecificationException {
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
        this.queryTransformerFactory = injector.getInstance(QueryTransformerFactory.class);
        this.termFactory = injector.getInstance(TermFactory.class);

        Mapping saturatedMapping = specification.getSaturatedMapping();
        ImmutableList<IQ> mappingEntriesIQs = saturatedMapping.getRDFAtomPredicates().stream()
                .map(saturatedMapping::getQueries)
                .flatMap(Collection::stream)
                .map(iq -> removeDistincts(iq, materializationParams.areDuplicatesAllowed()))
                .map(this::splitPotentialUnionNode)
                .flatMap(Collection::stream)
                .map(this::splitPotentialUnionNode)
                .flatMap(Collection::stream)
                .map(IQ::normalizeForOptimization)
                .collect(ImmutableCollectors.toList());

        ImmutableList<MappingEntryCluster> tmpMappingClusters = mappingEntriesIQs.stream()
                .map(this::createMappingEntryCluster)
                .collect(ImmutableCollectors.toList());

        mappingClusters = mergeMappingEntryClusters(tmpMappingClusters);
    }

    @Override
    public MaterializedGraphResultSet materialize() {
        return new OnePassMaterializedGraphResultSet(vocabulary,
                mappingClusters,
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
        throw new UnsupportedOperationException("To materialize different classes/properties in separate files, use the legacy materializer instead.");
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
        var vocabularyPropertyStream = mapping.getRDFProperties(tripleOrQuadPredicate).stream()
                .map(p -> new VocabularyEntry(p, 2));

        var vocabularyClassStream = mapping.getRDFClasses(tripleOrQuadPredicate).stream()
                .map(p -> new VocabularyEntry(p, 1));
        return Stream.concat(vocabularyClassStream, vocabularyPropertyStream);
    }

    private ImmutableList<MappingEntryCluster> mergeMappingEntryClusters(ImmutableList<MappingEntryCluster> mappingEntryClusters) {
        var complexMappingEntryCluster = mappingEntryClusters.stream()
                .filter(m -> m instanceof ComplexMappingEntryCluster)
                .collect(ImmutableCollectors.toList());


        var groupedByJoinRelationsEntries = mappingEntryClusters.stream()
                .filter(m -> m instanceof JoinMappingEntryCluster)
                .map(m -> Map.entry(
                        m.getDataNodes().stream()
                                .map(node -> node.getRelationDefinition().getAtomPredicate().getName())
                                .collect(ImmutableCollectors.toSet()),
                        m))
                .collect(ImmutableCollectors.toMultimap())
                .asMap().values().stream()
                .map(ImmutableList::copyOf)
                .collect(ImmutableCollectors.toList());

        var groupedBySingleRelationEntries = mappingEntryClusters.stream()
                .filter(m -> !(m instanceof ComplexMappingEntryCluster || m instanceof JoinMappingEntryCluster))
                .map(m -> Map.entry(
                        m.getDataNodes().get(0).getRelationDefinition().getAtomPredicate().getName(),
                        m))
                .collect(ImmutableCollectors.toMultimap())
                .asMap().values().stream()
                .map(ImmutableList::copyOf)
                .collect(ImmutableCollectors.toList());

        return ImmutableList.<MappingEntryCluster>builder()
                .addAll(mergeCompatibleClusters(groupedByJoinRelationsEntries))
                .addAll(mergeCompatibleClusters(groupedBySingleRelationEntries))
                .addAll(complexMappingEntryCluster)
                .build();
    }

    private MappingEntryCluster createMappingEntryCluster(IQ entryIQ) {
        IQTree tree = entryIQ.getTree();
        RDFFactTemplates rdfTemplates = new RDFFactTemplatesImpl(ImmutableList.of((entryIQ.getProjectionAtom().getArguments())));

        if (hasNotSupportedNode(tree)) {
            return new ComplexMappingEntryCluster(tree,
                    rdfTemplates,
                    entryIQ.getVariableGenerator(),
                    iqFactory,
                    substitutionFactory,
                    termFactory);
        }

        if (hasFilterOrJoinNode(tree)) {
            if (tree.getChildren().get(0).getRootNode() instanceof JoinLikeNode) {
                return new JoinMappingEntryCluster(
                        tree,
                        rdfTemplates,
                        entryIQ.getVariableGenerator(),
                        iqFactory,
                        substitutionFactory,
                        termFactory);
            } else if (tree.getChildren().get(0).getRootNode() instanceof FilterNode) {
                return new FilterMappingEntryCluster(
                        tree,
                        rdfTemplates,
                        entryIQ.getVariableGenerator(),
                        iqFactory,
                        termFactory,
                        substitutionFactory,
                        queryTransformerFactory);
            } else {
                return new ComplexMappingEntryCluster(tree,
                        rdfTemplates,
                        entryIQ.getVariableGenerator(),
                        iqFactory,
                        substitutionFactory,
                        termFactory);
            }
        }

        if ( !(tree.getChildren().get(0).getRootNode() instanceof ExtensionalDataNode)) {
            return new ComplexMappingEntryCluster(tree,
                    rdfTemplates,
                    entryIQ.getVariableGenerator(),
                    iqFactory,
                    substitutionFactory,
                    termFactory);
        }

        var extensionalNode = (ExtensionalDataNode) tree.getChildren().get(0).getRootNode();
        var argumentMap = extensionalNode.getArgumentMap();
        if (argumentMap.values().stream().allMatch(v -> v instanceof Variable)) {
            return new SimpleMappingEntryCluster(
                    tree,
                    rdfTemplates,
                    entryIQ.getVariableGenerator(),
                    iqFactory,
                    substitutionFactory,
                    termFactory);
        } else if (argumentMap.values().stream().anyMatch(v -> v instanceof DBConstant)) {
            //return new ComplexMappingEntryCluster(tree, rdfTemplates, entryIQ.getVariableGenerator(), iqFactory, substitutionFactory, termFactory);
            return new DictionaryPatternMappingEntryCluster(tree,
                    rdfTemplates,
                    entryIQ.getVariableGenerator(),
                    iqFactory,
                    substitutionFactory,
                    termFactory);

        } else {
            return new ComplexMappingEntryCluster(tree,
                    rdfTemplates,
                    entryIQ.getVariableGenerator(),
                    iqFactory,
                    substitutionFactory,
                    termFactory);
        }
    }

    private ImmutableList<IQ> splitPotentialUnionNode(IQ iq) {
        var tree = iq.getTree();
        if (!(tree.getRootNode() instanceof ConstructionNode)) {
            throw new MinorOntopInternalBugException("The root node of a mapping is expected to be a ConstructionNode");
        }
        if (tree.getChildren().get(0).getRootNode() instanceof UnionNode) {
            var unionChildren = tree.getChildren().get(0).getChildren();
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

    private ImmutableList<MappingEntryCluster> mergeCompatibleClusters(
            ImmutableList<ImmutableList<MappingEntryCluster>> compatibleClusters) {

        ArrayList<MappingEntryCluster> unmergedClusterEntries = new ArrayList<>(); // mutable!

        var mergedClusters = compatibleClusters.stream()
                .map( sameRelationClusterEntries -> {
                    var mergedSameRelationEntries = sameRelationClusterEntries.get(0);
                    for (int i=1; i<sameRelationClusterEntries.size(); i++) {
                        var m1 = sameRelationClusterEntries.get(i);
                        var merged = mergedSameRelationEntries.merge(m1);
                        if (merged.isPresent()) {
                            mergedSameRelationEntries = merged.get();
                        } else {
                            unmergedClusterEntries.add(m1);
                        }
                    }
                    return mergedSameRelationEntries;
                })
                .collect(ImmutableCollectors.toList());

        return ImmutableList.<MappingEntryCluster>builder()
                .addAll(mergedClusters)
                .addAll(unmergedClusterEntries)
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
    private boolean hasFilterOrJoinNode(IQTree tree) {
        if (tree.getRootNode() instanceof JoinOrFilterNode){
            return true;
        } else {
            return tree.getChildren().stream()
                    .anyMatch(this::hasFilterOrJoinNode);
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
