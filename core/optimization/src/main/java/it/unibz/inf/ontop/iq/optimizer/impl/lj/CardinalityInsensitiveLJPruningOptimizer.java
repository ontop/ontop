package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.impl.CaseInsensitiveIQTreeTransformerAdapter;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;
import java.util.Set;

/**
 * Prunes right children when their variables are not used outside the LJ
 * in a cardinality-insensitive context
 */
@Singleton
public class CardinalityInsensitiveLJPruningOptimizer extends DelegatingIQTreeVariableGeneratorTransformer implements IQTreeVariableGeneratorTransformer {

    private final IntermediateQueryFactory iqFactory;

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    protected CardinalityInsensitiveLJPruningOptimizer(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();

        this.transformer = IQTreeVariableGeneratorTransformer.of(new CaseInsensitiveIQTreeTransformerAdapter(iqFactory) {
            @Override
            protected IQTree transformCardinalityInsensitiveTree(IQTree tree) {
                IQVisitor<IQTree> transformer = new CardinalityInsensitiveLJPruningTransformer(
                        IQTreeTransformer.of(this),
                        tree.getVariables());
                return tree.acceptVisitor(transformer);
            }
        });
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }

    // TODO: unclear why it's called non-recursive
    private class CardinalityInsensitiveLJPruningTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final IQTreeTransformer lookForDistinctTransformer;
        private final ImmutableSet<Variable> variablesUsedByAncestors;

        CardinalityInsensitiveLJPruningTransformer(IQTreeTransformer lookForDistinctTransformer,
                                                             ImmutableSet<Variable> variablesUsedByAncestors) {
            super(CardinalityInsensitiveLJPruningOptimizer.this.iqFactory);
            this.lookForDistinctTransformer = lookForDistinctTransformer;
            this.variablesUsedByAncestors = variablesUsedByAncestors;
        }

        private CardinalityInsensitiveLJPruningTransformer getTransformer(Set<Variable> additionalVariables) {
            return variablesUsedByAncestors.containsAll(additionalVariables)
                    ? this
                    : new CardinalityInsensitiveLJPruningTransformer(lookForDistinctTransformer,
                                Sets.union(variablesUsedByAncestors, additionalVariables).immutableCopy());
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private ImmutableSet<Variable> getVariables(Optional<ImmutableExpression> optionalExpression) {
            return optionalExpression.map(ImmutableFunctionalTerm::getVariables).orElseGet(ImmutableSet::of);
        }

        public IQTree defaultTransformUnaryNode(UnaryIQTree tree) {
            return lookForDistinctTransformer.transform(tree);
        }

        @Override
        public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
            return defaultTransformUnaryNode(tree);
        }

        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode rootNode, IQTree child) {
            return defaultTransformUnaryNode(tree);
        }

        @Override
        public IQTree transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
            return defaultTransformUnaryNode(tree);
        }

        @Override
        public IQTree transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
            return defaultTransformUnaryNode(tree);
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            var newTransformer = getTransformer(rootNode.getLocallyRequiredVariables());
            IQTree newChild = newTransformer.transformChild(child);
            return withTransformedChild(tree, newChild);
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            var newTransformer = getTransformer(getVariables(rootNode.getOptionalFilterCondition()));
            IQTree newChild = newTransformer.transformChild(child);
            return withTransformedChild(tree, newChild);
        }

        @Override
        public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            IQTree newChild = transformChild(child);
            return withTransformedChild(tree, newChild);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            var treeVariables = tree.getVariables();
            if (treeVariables.isEmpty()
                    || leftChild.getVariables().containsAll(Sets.intersection(variablesUsedByAncestors, treeVariables)))
                // Prunes the right child
                return transformChild(leftChild);

            var commonVariables = BinaryNonCommutativeIQTreeTools.commonVariables(leftChild, rightChild);
            var newTransformer = getTransformer(Sets.union(commonVariables,
                    getVariables(rootNode.getOptionalFilterCondition())));

            var newLeft = newTransformer.transformChild(leftChild);
            var newRight = newTransformer.transformChild(rightChild);

            return withTransformedChildren(tree, newLeft, newRight);
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);
            return withTransformedChildren(tree, newChildren);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            var newTransformer = getTransformer(getVariables(rootNode.getOptionalFilterCondition()));
            ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children, newTransformer::transformChild);
            return withTransformedChildren(tree, newChildren);
        }
    }
}
