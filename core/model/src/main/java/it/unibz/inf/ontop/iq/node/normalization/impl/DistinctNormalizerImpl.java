package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class DistinctNormalizerImpl implements DistinctNormalizer {

    private static final int MAX_ITERATIONS = 10000;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private DistinctNormalizerImpl(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public IQTree normalizeForOptimization(DistinctNode distinctNode, IQTree child,
                                           VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree newChild = child.removeDistincts();
        return liftBinding(distinctNode, newChild, variableGenerator, currentIQProperties);
    }

    private IQTree liftBinding(DistinctNode distinctNode, IQTree child, VariableGenerator variableGenerator,
                               IQProperties currentIQProperties) {
        IQTree newChild = child.normalizeForOptimization(variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        if (newChildRoot instanceof ConstructionNode)
            return liftBindingConstructionChild((ConstructionNode) newChildRoot, currentIQProperties,
                    (UnaryIQTree) newChild, variableGenerator);
        else if (newChildRoot instanceof EmptyNode)
            return newChild;
        else
            return iqFactory.createUnaryIQTree(distinctNode, newChild,
                    currentIQProperties.declareNormalizedForOptimization());
    }

    private IQTree liftBindingConstructionChild(ConstructionNode constructionNode,
                                                IQProperties currentIQProperties, UnaryIQTree child,
                                                VariableGenerator variableGenerator) {
        // Non-final
        BindingLiftState state = new BindingLiftState(constructionNode, child.getChild(), variableGenerator);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            BindingLiftState newState = state.liftBindings();

            if (newState.equals(state))
                return newState.createNormalizedTree(currentIQProperties);
            state = newState;
        }
        throw new MinorOntopInternalBugException("DistinctNormalizerImpl.liftBindingConstructionChild() " +
                "did not converge after " + MAX_ITERATIONS);
    }

    private class BindingLiftState {
        // Parent first
        private final ImmutableList<ConstructionNode> ancestors;
        // First descendent tree not starting with a construction node
        private final IQTree grandChildTree;
        @Nullable
        private final ConstructionNode childConstructionNode;
        private final VariableGenerator variableGenerator;

        /**
         * Initial state
         */
        public BindingLiftState(@Nonnull ConstructionNode childConstructionNode, IQTree grandChildTree,
                                VariableGenerator variableGenerator) {
            this.ancestors = ImmutableList.of();
            this.grandChildTree = grandChildTree;
            this.childConstructionNode = childConstructionNode;
            this.variableGenerator = variableGenerator;
        }

        private BindingLiftState(ImmutableList<ConstructionNode> ancestors, IQTree grandChildTree,
                                 VariableGenerator variableGenerator) {
            this.ancestors = ancestors;
            this.grandChildTree = grandChildTree;
            this.childConstructionNode = null;
            this.variableGenerator = variableGenerator;
        }

        private BindingLiftState(ImmutableList<ConstructionNode> ancestors, IQTree grandChildTree,
                                 VariableGenerator variableGenerator,
                                 @Nonnull ConstructionNode childConstructionNode) {
            this.ancestors = ancestors;
            this.grandChildTree = grandChildTree;
            this.childConstructionNode = childConstructionNode;
            this.variableGenerator = variableGenerator;
        }

        public Optional<ConstructionNode> getChildConstructionNode() {
            return Optional.ofNullable(childConstructionNode);
        }

        public BindingLiftState liftBindings() {
            if (childConstructionNode == null)
                return this;

            ImmutableSubstitution<ImmutableTerm> initialSubstitution = childConstructionNode.getSubstitution();
            if (initialSubstitution.isEmpty())
                return this;

            VariableNullability grandChildVariableNullability = grandChildTree.getVariableNullability();

            ImmutableMap<Boolean, ImmutableMap<Variable, ImmutableTerm>> partition =
                    initialSubstitution.getImmutableMap().entrySet().stream()
                            .collect(ImmutableCollectors.partitioningBy(
                                    e -> isLiftable(e.getValue(), grandChildVariableNullability),
                                    ImmutableCollectors.toMap()));

            Optional<ConstructionNode> liftedConstructionNode = Optional.ofNullable(partition.get(true))
                    .filter(m -> !m.isEmpty())
                    .map(substitutionFactory::getSubstitution)
                    .map(s -> iqFactory.createConstructionNode(childConstructionNode.getVariables(), s));

            ImmutableSet<Variable> newChildVariables = liftedConstructionNode
                    .map(ConstructionNode::getChildVariables)
                    .orElseGet(childConstructionNode::getVariables);

            Optional<ConstructionNode> newConstructionNode = Optional.ofNullable(partition.get(false))
                    .filter(m -> !m.isEmpty())
                    .map(substitutionFactory::getSubstitution)
                    .map(s -> iqFactory.createConstructionNode(newChildVariables, s))
                    .map(Optional::of)
                    .orElseGet(() -> newChildVariables.equals(grandChildTree.getVariables())
                            ? Optional.empty()
                            : Optional.of(iqFactory.createConstructionNode(newChildVariables)));

            // Nothing lifted
            if (newConstructionNode
                    .filter(n -> n.isEquivalentTo(childConstructionNode))
                    .isPresent()) {
                if (liftedConstructionNode.isPresent())
                    throw new MinorOntopInternalBugException("Unexpected lifted construction node");
                return this;
            }

            ImmutableList<ConstructionNode> newAncestors = liftedConstructionNode
                    .map(n -> Stream.concat(ancestors.stream(), Stream.of(n))
                            .collect(ImmutableCollectors.toList()))
                    .orElseThrow(() -> new MinorOntopInternalBugException("A lifted construction node was expected"));

            return newConstructionNode
                    .map(c -> new BindingLiftState(newAncestors, grandChildTree, variableGenerator, c))
                    .orElseGet(() -> new BindingLiftState(newAncestors, grandChildTree, variableGenerator));
        }

        public IQTree createNormalizedTree(IQProperties currentIQProperties) {
            IQTree newChildTree = Optional.ofNullable(childConstructionNode)
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, grandChildTree,
                            iqFactory.createIQProperties().declareNormalizedForOptimization()))
                    .orElse(grandChildTree);

            IQTree distinctTree = iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(), newChildTree,
                    currentIQProperties.declareNormalizedForOptimization());

            return ancestors.reverse().stream()
                    .reduce(distinctTree,
                            (t, a) -> iqFactory.createUnaryIQTree(a, t),
                            (t1, t2) -> { throw new MinorOntopInternalBugException("No merge was expected"); })
                    // Recursive (for merging top construction nodes)
                    .normalizeForOptimization(variableGenerator);
        }

        /**
         *
         * NULL is treated as a regular constant (consistent with SPARQL DISTINCT and apparently with SQL DISTINCT)
         *
         */
        private boolean isLiftable(ImmutableTerm value, VariableNullability variableNullability) {
            if (value instanceof VariableOrGroundTerm)
                return true;
            // TODO: refactor
            return ((ImmutableFunctionalTerm) value).getFunctionSymbol()
                    .isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms();
            //return ((ImmutableFunctionalTerm) value).isInjective(variableNullability);
        }
    }

}
