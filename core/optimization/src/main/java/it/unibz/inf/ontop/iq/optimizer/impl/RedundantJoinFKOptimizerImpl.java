package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.optimizer.RedundantJoinFKOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Stream;

public class RedundantJoinFKOptimizerImpl implements RedundantJoinFKOptimizer {

    private final RedundantJoinFKTransformer transformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private RedundantJoinFKOptimizerImpl(CoreSingletons coreSingletons) {
        this.transformer = new RedundantJoinFKTransformer(coreSingletons);
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree newTree = transformer.transform(query.normalizeForOptimization()
                .getTree());

        return newTree.equals(query.getTree())
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }

    protected static class RedundantJoinFKTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected final TermFactory termFactory;

        protected RedundantJoinFKTransformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
            this.termFactory = coreSingletons.getTermFactory();
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> initialChildren) {
            ImmutableList<IQTree> liftedChildren = initialChildren.stream()
                    .map(t -> t.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            ImmutableMap<Boolean, ImmutableList<IQTree>> childPartitions = liftedChildren.stream()
                    .collect(ImmutableCollectors.partitioningBy(n -> (n instanceof ExtensionalDataNode)));

            Optional<ImmutableList<IQTree>> optionalExtensionalChildren = Optional.ofNullable(childPartitions.get(true));

            if (!optionalExtensionalChildren.isPresent())
                return liftedChildren.equals(initialChildren)
                        ? tree
                        : iqFactory.createNaryIQTree(rootNode, liftedChildren);


            return optimizeExtensionalChildren(
                    (ImmutableList<ExtensionalDataNode>)(ImmutableList<?>)optionalExtensionalChildren.get())
                    .map(extensionalChildren -> buildNewTree(rootNode, extensionalChildren,
                            Optional.ofNullable(childPartitions.get(false)).orElseGet(ImmutableList::of)))
                    .orElseGet(() -> liftedChildren.equals(initialChildren)
                            ? tree
                            : iqFactory.createNaryIQTree(rootNode, liftedChildren));
        }

        protected Optional<IQTree> optimizeExtensionalChildren(ImmutableList<ExtensionalDataNode> extensionalChildren) {
            ImmutableMap<RelationDefinition, Collection<ExtensionalDataNode>> dataNodeMap = extensionalChildren.stream()
                    .collect(ImmutableCollectors.toMultimap(
                            ExtensionalDataNode::getRelationDefinition,
                            c -> c)).asMap();

            ImmutableSet<ExtensionalDataNode> redundantNodes = extractRedundantNodes(dataNodeMap);

            if (redundantNodes.isEmpty())
                return Optional.empty();

            Optional<ImmutableExpression> newConditions = termFactory.getDBIsNotNull(redundantNodes.stream()
                    .flatMap(n -> n.getVariables().stream())
                    .distinct());

            ImmutableList<IQTree> remainingChildren = extensionalChildren.stream()
                    .filter(n -> !redundantNodes.contains(n))
                    .collect(ImmutableCollectors.toList());

            switch (remainingChildren.size()) {
                case 0:
                    throw new IllegalStateException("At least one child must remain");
                case 1:
                    return Optional.of(
                            newConditions
                                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(
                                            iqFactory.createFilterNode(c),
                                            remainingChildren.get(0)))
                                    .orElseGet(() -> remainingChildren.get(0)));
                default:
                    return Optional.of(
                            iqFactory.createNaryIQTree(
                                    iqFactory.createInnerJoinNode(newConditions),
                                    remainingChildren));
            }
        }

        /**
         * Limitations: assumes that FK transitive closure has already been done
         */
        private ImmutableSet<ExtensionalDataNode> extractRedundantNodes(
                ImmutableMap<RelationDefinition, Collection<ExtensionalDataNode>> dataNodeMap) {

            // Mutable . Used for avoiding FK loops
            Set<ExtensionalDataNode> redundantNodes = new HashSet<>();

            for (Map.Entry<RelationDefinition, Collection<ExtensionalDataNode>> entry: dataNodeMap.entrySet()) {
                redundantNodes.addAll(entry.getKey().getForeignKeys().stream()
                        .flatMap(fk -> Optional.ofNullable(dataNodeMap.get(fk.getReferencedRelation()))
                                .map(Collection::stream)
                                .orElseGet(Stream::empty)
                                .filter(targetNode -> isJustHavingFKArguments(fk, targetNode))
                                .filter(targetNode -> entry.getValue().stream()
                                                .anyMatch(s -> (!redundantNodes.contains(s))
                                                        && isSafeAndTargetMatching(fk, s, targetNode))))
                        .collect(ImmutableCollectors.toSet()));
            }

            return ImmutableSet.copyOf(redundantNodes);
        }

        private boolean isJustHavingFKArguments(ForeignKeyConstraint foreignKeyConstraint, ExtensionalDataNode targetNode) {
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> targetArgumentMap = targetNode.getArgumentMap();

            // The only arguments on the target part should be the one of FK
            return targetArgumentMap.keySet().equals(
                    foreignKeyConstraint.getComponents().stream()
                            .map(c -> c.getReferencedAttribute().getIndex() -1)
                            .collect(ImmutableCollectors.toSet()));
        }

        private boolean isSafeAndTargetMatching(ForeignKeyConstraint foreignKeyConstraint,
                                                ExtensionalDataNode sourceNode, ExtensionalDataNode targetNode) {
            // Protection against FKs like from a PK to itself
            if (sourceNode.equals(targetNode))
                return false;

            ImmutableMap<Integer, ? extends VariableOrGroundTerm> sourceArgumentMap = sourceNode.getArgumentMap();

            if (!foreignKeyConstraint.getComponents().stream()
                    .map(c -> c.getAttribute().getIndex() -1)
                    .allMatch(sourceArgumentMap::containsKey))
                return false;

            ImmutableMap<Integer, ? extends VariableOrGroundTerm> targetArgumentMap = targetNode.getArgumentMap();

            return foreignKeyConstraint.getComponents().stream()
                    .allMatch(c -> sourceArgumentMap.get(c.getAttribute().getIndex() -1).equals(
                            targetArgumentMap.get(c.getReferencedAttribute().getIndex() -1)));
        }

        /**
         * The returned tree may not be normalized (to be done at the IQOptimizer level)
         */
        private IQTree buildNewTree(InnerJoinNode rootNode, IQTree optimizedChildTree,
                                    ImmutableList<IQTree> otherChildren) {
            ImmutableList<IQTree> newChildren = Stream.concat(Stream.of(optimizedChildTree), otherChildren.stream())
                    .collect(ImmutableCollectors.toList());

            switch(newChildren.size()) {
                case 0:
                    throw new IllegalStateException("The optimization should not eliminate all the children");
                case 1:
                    return rootNode.getOptionalFilterCondition().isPresent()
                            ? iqFactory.createUnaryIQTree(
                                iqFactory.createFilterNode((rootNode.getOptionalFilterCondition().get())),
                                newChildren.get(0))
                            : newChildren.get(0);
                default:
                    return iqFactory.createNaryIQTree(rootNode, newChildren);
            }
        }


    }

}
