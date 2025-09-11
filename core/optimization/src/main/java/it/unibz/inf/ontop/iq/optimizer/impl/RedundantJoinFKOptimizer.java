package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Stream;

public class RedundantJoinFKOptimizer extends DelegatingIQTreeVariableGeneratorTransformer implements IQTreeVariableGeneratorTransformer {

    private final IQTreeTools iqTreeTools;
    private final TermFactory termFactory;
    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    private RedundantJoinFKOptimizer(CoreSingletons coreSingletons) {
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.termFactory = coreSingletons.getTermFactory();

        this.transformer = IQTreeVariableGeneratorTransformer.of(new DefaultRecursiveIQTreeVisitingInnerJoinTransformer(
                coreSingletons.getIQFactory(),
                new RedundantJoinFKTransformer()));
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }

    private class RedundantJoinFKTransformer implements InnerJoinTransformer {

        @Override
        public Optional<IQTree> transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> liftedChildren) {
            ImmutableMap<Boolean, ImmutableList<IQTree>> childPartitions = liftedChildren.stream()
                    .collect(ImmutableCollectors.partitioningBy(n -> (n instanceof ExtensionalDataNode)));

            ImmutableList<ExtensionalDataNode> extensionalChildren = (ImmutableList)childPartitions.get(true);
            assert extensionalChildren != null;
            var otherChildren = childPartitions.get(false);
            assert otherChildren != null;

            var optimisedExtensionalChildren = optimizeExtensionalChildren(extensionalChildren);
            if (optimisedExtensionalChildren.isEmpty())
                return Optional.empty();

            // The returned tree may not be normalized (to be done at the IQOptimizer level)
            return Optional.of(iqTreeTools.createOptionalInnerJoinTree(
                            rootNode.getOptionalFilterCondition(),
                            Stream.concat(optimisedExtensionalChildren.stream(), otherChildren.stream())
                                    .collect(ImmutableCollectors.toList()))
                    .orElseThrow(() -> new IllegalStateException("The optimization should not eliminate all the children")));
        }

        private Optional<IQTree> optimizeExtensionalChildren(ImmutableList<ExtensionalDataNode> extensionalChildren) {

            ImmutableSet<ExtensionalDataNode> redundantNodes = extractRedundantNodes(extensionalChildren);

            if (redundantNodes.isEmpty())
                return Optional.empty();

            Optional<ImmutableExpression> newConditions = termFactory.getDBIsNotNull(
                    redundantNodes.stream()
                            .flatMap(n -> n.getVariables().stream())
                            .distinct());

            ImmutableList<ExtensionalDataNode> remainingChildren = extensionalChildren.stream()
                    .filter(n -> !redundantNodes.contains(n))
                    .collect(ImmutableCollectors.toList());

            return Optional.of(iqTreeTools.createOptionalInnerJoinTree(newConditions, remainingChildren)
                    .orElseThrow(() -> new IllegalStateException("At least one child must remain")));
        }

        /**
         * Limitations: assumes that FK transitive closure has already been done
         */
        private ImmutableSet<ExtensionalDataNode> extractRedundantNodes(ImmutableList<ExtensionalDataNode> extensionalChildren) {

            ImmutableMultimap<RelationDefinition, ExtensionalDataNode> dataNodeMultimap = extensionalChildren.stream()
                    .collect(ImmutableCollectors.toMultimap(
                            ExtensionalDataNode::getRelationDefinition,
                            c -> c));

            // Mutable . Used for avoiding FK loops
            Set<ExtensionalDataNode> redundantNodes = new HashSet<>();

            for (Map.Entry<RelationDefinition, Collection<ExtensionalDataNode>> entry: dataNodeMultimap.asMap().entrySet()) {
                redundantNodes.addAll(entry.getKey().getForeignKeys().stream()
                        .flatMap(fk -> dataNodeMultimap.get(fk.getReferencedRelation()).stream()
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
    }
}
