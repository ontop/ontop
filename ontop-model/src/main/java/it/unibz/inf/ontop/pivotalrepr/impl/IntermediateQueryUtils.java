package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.model.impl.NeutralSubstitution;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.utils.FunctionalTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Optional;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

/**
 * TODO: explain
 */
public class IntermediateQueryUtils {

    /**
     * This class can be derived to construct more specific builders.
     */
    protected IntermediateQueryUtils () {
    }

    /**
     * TODO: describe
     */
    public static Optional<IntermediateQuery> mergeDefinitions(IntermediateQueryFactory iqFactory,
                                                               Collection<IntermediateQuery> predicateDefinitions) {
        return mergeDefinitions(iqFactory, predicateDefinitions, Optional.empty());
    }


    /**
     * TODO: describe
     * The optional modifiers are for the top construction node above the UNION (if any).
     *
     * TODO: refactor it so that the definitive intermediate query is directly constructed.
     */
    public static Optional<IntermediateQuery> mergeDefinitions(IntermediateQueryFactory iqFactory,
                                                               Collection<IntermediateQuery> predicateDefinitions,
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
                    InjectiveVar2VarSubstitution disjointVariableSetRenaming = generateNotConflictingRenaming(
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

                    appendDefinition(iqFactory, queryBuilder, unionNode, def, renamingSubstitution);
                });

        return Optional.of(queryBuilder.build());
    }

    /**
     * When such substitution DO NOT EXIST, returns an EMPTY OPTIONAL.
     * When NO renaming is NEEDED returns an EMPTY SUBSTITUTION.
     *
     */
    public static Optional<InjectiveVar2VarSubstitution> computeRenamingSubstitution(
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

            return Optional.of(DATA_FACTORY.getInjectiveVar2VarSubstitution(newMap));
        }
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
        queryBuilder.addChild(topUnionNode, subQuery.getRootConstructionNode());

        subQuery.getNodesInTopDownOrder().stream()
                .skip(1)
                .forEach(node -> queryBuilder.addChild(
                        subQuery.getParent(node).orElseThrow(()-> new IllegalStateException("Unknown parent")),
                        node,
                        subQuery.getOptionalPosition(node)));
    }

    public static InjectiveVar2VarSubstitution generateNotConflictingRenaming(VariableGenerator variableGenerator,
                                                                               ImmutableSet<Variable> variables) {
        ImmutableMap<Variable, Variable> newMap = variables.stream()
                .map(v -> new AbstractMap.SimpleEntry<>(v, variableGenerator.generateNewVariableIfConflicting(v)))
                .filter(pair -> ! pair.getKey().equals(pair.getValue()))
                .collect(ImmutableCollectors.toMap());

        return DATA_FACTORY.getInjectiveVar2VarSubstitution(newMap);
    }

    /**
     * Appends a definition under the union node after renaming it.
     */
    private static void appendDefinition(IntermediateQueryFactory iqFactory,
                                         IntermediateQueryBuilder queryBuilder, UnionNode unionNode,
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

        queryBuilder.addChild(unionNode, renamedNodeMap.get(definition.getRootConstructionNode()));

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
     * TODO: explain
     *
     */
    public static IntermediateQueryBuilder convertToBuilder(IntermediateQuery originalQuery)
            throws IntermediateQueryBuilderException {
        ImmutableList<QueryNode> originalNodesInTopDownOrder = originalQuery.getNodesInTopDownOrder();

        ImmutableMap<QueryNode, QueryNode> cloneNodeMap = originalNodesInTopDownOrder.stream()
                .map(n -> new AbstractMap.SimpleEntry<>(n, n.clone()))
                .collect(ImmutableCollectors.toMap());

        IntermediateQueryBuilder queryBuilder = originalQuery.newBuilder();
        queryBuilder.init(originalQuery.getProjectionAtom(),
                (ConstructionNode) cloneNodeMap.get(originalQuery.getRootConstructionNode()));

        originalNodesInTopDownOrder.stream()
                .skip(1)
                .forEach(node -> queryBuilder.addChild(cloneNodeMap.get(originalQuery.getParent(node)
                        .orElseThrow(() -> new IllegalStateException("Unknown parent"))),
                        cloneNodeMap.get(node)));

        return queryBuilder;
    }


}
