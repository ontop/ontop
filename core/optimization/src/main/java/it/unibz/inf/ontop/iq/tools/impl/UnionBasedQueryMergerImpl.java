package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ImmutableQueryModifiers;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.optimizer.BindingLiftOptimizer;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.optimizer.ConstructionNodeCleaner;
import it.unibz.inf.ontop.iq.optimizer.FlattenUnionOptimizer;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.substitution.impl.NeutralSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.QueryNodeRenamer;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.utils.FunctionalTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.SUBSTITUTION_FACTORY;

@Singleton
public class UnionBasedQueryMergerImpl implements UnionBasedQueryMerger {

    private final IntermediateQueryFactory iqFactory;
    private final BindingLiftOptimizer bindingLifter;
    private final ConstructionNodeCleaner constructionNodeCleaner;
    private final FlattenUnionOptimizer unionFlattener;

    @Inject
    private UnionBasedQueryMergerImpl(IntermediateQueryFactory iqFactory, BindingLiftOptimizer bindingLifter,
                                      ConstructionNodeCleaner constructionNodeCleaner,
                                      FlattenUnionOptimizer unionFlattener) {
        this.iqFactory = iqFactory;
        this.bindingLifter = bindingLifter;
        this.constructionNodeCleaner = constructionNodeCleaner;
        this.unionFlattener = unionFlattener;
    }

    @Override
    public Optional<IntermediateQuery> mergeDefinitions(Collection<IntermediateQuery> predicateDefinitions) {
        return mergeDefinitions(predicateDefinitions, Optional.empty());
    }

    @Override
    public Optional<IntermediateQuery> mergeDefinitions(Collection<IntermediateQuery> predicateDefinitions,
                                                        ImmutableQueryModifiers topModifiers) {
        return mergeDefinitions(predicateDefinitions, Optional.of(topModifiers));
    }

    /**
     * The optional modifiers are for the top construction node above the UNION (if any).
     *
     * TODO: refactor it so that the definitive intermediate query is directly constructed.
     */
    private Optional<IntermediateQuery> mergeDefinitions(Collection<IntermediateQuery> predicateDefinitions,
                                                         Optional<ImmutableQueryModifiers> optionalTopModifiers) {
        if (predicateDefinitions.isEmpty())
            return Optional.empty();

        IntermediateQuery firstDefinition = predicateDefinitions.iterator().next();
        if (predicateDefinitions.size() == 1) {
            return Optional.of(firstDefinition);
        }

        DistinctVariableOnlyDataAtom projectionAtom = firstDefinition.getProjectionAtom();

        ConstructionNode rootNode = iqFactory.createConstructionNode(projectionAtom.getVariables(),
                new NeutralSubstitution(), optionalTopModifiers);

        IntermediateQueryBuilder queryBuilder = firstDefinition.newBuilder();
        queryBuilder.init(projectionAtom, rootNode);

        UnionNode unionNode = iqFactory.createUnionNode(projectionAtom.getVariables());
        queryBuilder.addChild(rootNode, unionNode);

        // First definition can be added safely
        appendFirstDefinition(queryBuilder, unionNode, firstDefinition);

        VariableGenerator variableGenerator = new VariableGenerator(firstDefinition.getKnownVariables());

        predicateDefinitions.stream()
                .skip(1)
                .forEach(def -> {
                    // Updates the variable generator
                    InjectiveVar2VarSubstitution disjointVariableSetRenaming = SUBSTITUTION_FACTORY.generateNotConflictingRenaming(
                            variableGenerator, def.getKnownVariables());

                    ImmutableSet<Variable> freshVariables = ImmutableSet.copyOf(
                            disjointVariableSetRenaming.getImmutableMap().values());

                    InjectiveVar2VarSubstitution headSubstitution = computeRenamingSubstitution(
                            disjointVariableSetRenaming.applyToDistinctVariableOnlyDataAtom(def.getProjectionAtom()),
                            projectionAtom)
                            .orElseThrow(() -> new IllegalStateException("Bug: unexpected incompatible atoms"));

                    InjectiveVar2VarSubstitution renamingSubstitution =
                            /**
                             * fresh variables are excluded from the domain of the renaming substitution
                             *  since they are in use in the sub-query.
                             *
                             *  NB: this guarantees that the renaming substitution is injective
                             */
                            headSubstitution.composeWithAndPreserveInjectivity(disjointVariableSetRenaming, freshVariables)
                                    .orElseThrow(()-> new IllegalStateException("Bug: the renaming substitution is not injective"));

                    appendDefinition(queryBuilder, unionNode, def, renamingSubstitution);
                });


        return Optional.of(normalizeIQ(queryBuilder.build()));
    }

    /**
     * Appends the first definition which is known to BE SAFE.
     *
     * Side-effect on the queryBuilder
     *
     */
    private static void appendFirstDefinition(IntermediateQueryBuilder queryBuilder, UnionNode topUnionNode,
                                              IntermediateQuery subQuery) {

        // First add the root of the sub-query
        queryBuilder.addChild(topUnionNode, subQuery.getRootNode());

        subQuery.getNodesInTopDownOrder().stream()
                .skip(1)
                .forEach(node -> queryBuilder.addChild(
                        subQuery.getParent(node).orElseThrow(()-> new IllegalStateException("Unknown parent")),
                        node,
                        subQuery.getOptionalPosition(node)));
    }

    /**
     * Appends a definition under the union node after renaming it.
     */
    private void appendDefinition(IntermediateQueryBuilder queryBuilder, UnionNode unionNode,
                                  IntermediateQuery definition, InjectiveVar2VarSubstitution renamingSubstitution) {
        QueryNodeRenamer nodeRenamer = new QueryNodeRenamer(iqFactory, renamingSubstitution);
        ImmutableList<QueryNode> originalNodesInTopDownOrder = definition.getNodesInTopDownOrder();

        /**
         * Renames all the nodes (new objects) and maps them to original nodes
         */
        ImmutableMap<QueryNode, QueryNode> renamedNodeMap = originalNodesInTopDownOrder.stream()
                .map(n -> new AbstractMap.SimpleEntry<>(n, n.acceptNodeTransformer(nodeRenamer)))
                .collect(ImmutableCollectors.toMap());

        /**
         * Adds the renamed root of the definition
         */

        queryBuilder.addChild(unionNode, renamedNodeMap.get(definition.getRootNode()));

        /**
         * Add the other renamed nodes
         */
        originalNodesInTopDownOrder.stream()
                .skip(1)
                .forEach(node -> queryBuilder.addChild(
                        renamedNodeMap.get(definition.getParent(node)
                                .orElseThrow(()-> new IllegalStateException("Unknown parent"))),
                        renamedNodeMap.get(node),
                        definition.getOptionalPosition(node)));
    }

    /**
     * When such substitution DO NOT EXIST, returns an EMPTY OPTIONAL.
     * When NO renaming is NEEDED returns an EMPTY SUBSTITUTION.
     *
     */
    private static Optional<InjectiveVar2VarSubstitution> computeRenamingSubstitution(
            DistinctVariableOnlyDataAtom sourceProjectionAtom,
            DistinctVariableOnlyDataAtom targetProjectionAtom) {

        int arity = sourceProjectionAtom.getEffectiveArity();

        if (!sourceProjectionAtom.getPredicate().equals(targetProjectionAtom.getPredicate())
                || (arity != targetProjectionAtom.getEffectiveArity())) {
            return Optional.empty();
        }
        else {
            ImmutableMap<Variable, Variable> newMap = FunctionalTools.zip(
                    sourceProjectionAtom.getArguments(),
                    targetProjectionAtom.getArguments()).stream()
                    .distinct()
                    .filter(e -> !e.getKey().equals(e.getValue()))
                    .collect(ImmutableCollectors.toMap());

            return Optional.of(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(newMap));
        }
    }

    /**
     * Lift substitutions and query modifiers, and get rid of resulting idle construction nodes.
     * Then flatten nested unions.
     */
    private IntermediateQuery normalizeIQ(IntermediateQuery query) {
        try {
            IntermediateQuery queryAfterBindingLift = bindingLifter.optimize(query);
            IntermediateQuery queryAfterCNodeCleaning = constructionNodeCleaner.optimize(queryAfterBindingLift);
            return unionFlattener.optimize(queryAfterCNodeCleaning);
        }catch (EmptyQueryException e){
            throw new IllegalStateException("The query should not be emptied by applying this normalization");
        }
    }
}
