package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.TrueNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractBelowDistinctInnerJoinTransformer implements InnerJoinTransformer {

    protected final IntermediateQueryFactory iqFactory;
    protected final TermFactory termFactory;
    protected final IQTreeTools iqTreeTools;

    protected AbstractBelowDistinctInnerJoinTransformer(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
    }

    @Override
    public Optional<IQTree> transformInnerJoin(NaryIQTree tree, InnerJoinNode innerJoinNode,
                                               ImmutableList<IQTree> transformedChildren) {
        //Mutable
        final List<IQTree> currentChildren = Lists.newArrayList(transformedChildren);
        IntStream.range(0, transformedChildren.size())
                .filter(i -> isDetectedAsRedundant(
                        currentChildren.get(i),
                        IntStream.range(0, currentChildren.size())
                                .filter(j -> j != i)
                                .mapToObj(currentChildren::get)))
                // SIDE-EFFECT
                .forEach(i -> currentChildren.set(i, iqFactory.createTrueNode()));

        ImmutableSet<Variable> variablesToFilterNulls = IntStream.range(0, transformedChildren.size())
                .filter(i -> currentChildren.get(i).getRootNode() instanceof TrueNode)
                .mapToObj(transformedChildren::get)
                .map(IQTree::getVariables)
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toSet());

        Optional<ImmutableExpression> expression = termFactory.getConjunction(innerJoinNode.getOptionalFilterCondition(),
                variablesToFilterNulls.stream().map(termFactory::getDBIsNotNull));

        // NB: will be normalized later on
        return Optional.of(iqTreeTools.createInnerJoinTree(expression, ImmutableList.copyOf(currentChildren)));
    }

    /**
     * Should not return any false positive
     */
    protected abstract boolean isDetectedAsRedundant(IQTree child, Stream<IQTree> otherChildren);

    protected boolean isDetectedAsRedundant(ExtensionalDataNode dataNode, ExtensionalDataNode otherDataNode) {
        if (!dataNode.getRelationDefinition().equals(otherDataNode.getRelationDefinition()))
            return false;

        ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap = dataNode.getArgumentMap();
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> otherArgumentMap = otherDataNode.getArgumentMap();

        ImmutableSet<Integer> firstIndexes = argumentMap.keySet();
        ImmutableSet<Integer> otherIndexes = otherArgumentMap.keySet();

        Set<Integer> commonIndexes = Sets.intersection(firstIndexes, otherIndexes);

        return Sets.union(firstIndexes, otherIndexes).stream()
                .filter(i -> !(commonIndexes.contains(i) && argumentMap.get(i).equals(otherArgumentMap.get(i))))
                .noneMatch(argumentMap::containsKey);
    }
}
