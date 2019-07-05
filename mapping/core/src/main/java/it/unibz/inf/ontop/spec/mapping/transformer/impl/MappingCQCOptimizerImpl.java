package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.constraints.ImmutableCQ;
import it.unibz.inf.ontop.constraints.ImmutableCQContainmentCheck;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.ChildTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultNonRecursiveIQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.AND;

public class MappingCQCOptimizerImpl implements MappingCQCOptimizer {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;

    @Inject
    public MappingCQCOptimizerImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
    }

    @Override
    public IQ optimize(ImmutableCQContainmentCheck<RelationPredicate> cqContainmentCheck, IQ query) {

        IQTree tree0 = query.getTree().acceptTransformer(new DefaultRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
                ImmutableList<IQTree> joinChildren = children.stream().filter(c -> c.getRootNode() instanceof InnerJoinNode).collect(ImmutableCollectors.toList());

                ImmutableList<ImmutableExpression> filters = joinChildren.stream()
                        .map(c -> ((InnerJoinNode)c.getRootNode()).getOptionalFilterCondition())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(ImmutableCollectors.toList());

                return iqFactory.createNaryIQTree(
                        filters.isEmpty()
                            ? rootNode
                            : iqFactory.createInnerJoinNode(getConjunction(rootNode.getOptionalFilterCondition(), filters)),
                        Stream.concat(
                            children.stream().filter(c -> !(c.getRootNode() instanceof InnerJoinNode)),
                            joinChildren.stream().flatMap(c -> c.getChildren().stream()))
                                .map(t -> t.acceptTransformer(this))
                                .collect(ImmutableCollectors.toList()));
            }
        });

        IQTree tree = tree0.acceptTransformer(new FilterChildNormalizer());

        if (tree.getRootNode() instanceof ConstructionNode && tree.getChildren().size() == 1) {
            ConstructionNode constructionNode = (ConstructionNode)tree.getRootNode();
            IQTree joinTree = tree.getChildren().get(0);
            Optional<ImmutableList<ExtensionalDataNode>> c = IQ2CQ.getExtensionalDataNodes(joinTree);
            if (c.isPresent() && c.get().size() > 1) {
                InnerJoinNode joinNode = (InnerJoinNode) joinTree.getRootNode();
                ImmutableList<Variable> answerVariables = Stream.concat(
                        constructionNode.getSubstitution().getImmutableMap().values().stream()
                                .flatMap(ImmutableTerm::getVariableStream),
                        joinNode.getOptionalFilterCondition()
                                .map(ImmutableTerm::getVariableStream).orElse(Stream.of()))
                        .collect(ImmutableCollectors.toSet()).stream() // remove duplicates
                        .collect(ImmutableCollectors.toList());

                ImmutableList<ExtensionalDataNode> children = c.get();
                int currentIndex = 0;
                while (currentIndex < children.size()) {
                    ImmutableList.Builder<ExtensionalDataNode> builder = ImmutableList.builder();
                    for (int i = 0; i < children.size(); i++)
                        if (i != currentIndex)
                            builder.add(children.get(i));
                    ImmutableList<ExtensionalDataNode> subChildren = builder.build();

                    if (subChildren.stream()
                            .flatMap(a -> a.getVariables().stream())
                            .collect(ImmutableCollectors.toSet())
                            .containsAll(answerVariables)) {

                        if (cqContainmentCheck.isContainedIn(new ImmutableCQ<>(answerVariables, IQ2CQ.toDataAtoms(subChildren)), new ImmutableCQ<>(answerVariables, IQ2CQ.toDataAtoms(children)))) {
                            children = subChildren;
                            if (children.size() < 2)
                                break;
                            currentIndex = 0; // reset
                        }
                        else
                            currentIndex++;
                    }
                    else
                        currentIndex++;
                }

                return iqFactory.createIQ(
                        query.getProjectionAtom(),
                        iqFactory.createUnaryIQTree(
                                (ConstructionNode)tree.getRootNode(),
                                IQ2CQ.toIQTree(children, joinNode.getOptionalFilterCondition(), iqFactory)));
            }
        }
        return iqFactory.createIQ(query.getProjectionAtom(), tree);
    }

    // PINCHED FROM ExplicitEqualityTransformerImpl
    // TODO: extract as an independent class

    /**
     * Affects each outermost filter or (left) join n in the tree.
     * For each child of n, deletes its root if it is a filter node.
     * Then:
     * - if n is a join or filter: merge the boolean expressions
     * - if n is a left join: merge boolean expressions coming from the right, and lift the ones coming from the left.
     * This lift is only performed for optimization purposes: may save a subquery during SQL generation.
     */
    class FilterChildNormalizer extends DefaultNonRecursiveIQTreeTransformer {

        private final ChildTransformer childTransformer;

        public FilterChildNormalizer() {
            this.childTransformer = new ChildTransformer(iqFactory, this);
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {

            Optional<ImmutableExpression> leftChildChildExpression = getOptionalChildExpression(leftChild);
            Optional<ImmutableExpression> rightChildExpression = getOptionalChildExpression(rightChild);

            if (!leftChildChildExpression.isPresent() && !rightChildExpression.isPresent())
                return tree;

            IQTree leftJoinTree = iqFactory.createBinaryNonCommutativeIQTree(
                    rightChildExpression.isPresent()
                                ? iqFactory.createLeftJoinNode(getConjunction(
                                                        rootNode.getOptionalFilterCondition(),
                                                        ImmutableList.of(rightChildExpression.get())))
                                : rootNode,
                    trimRootFilter(leftChild),
                    trimRootFilter(rightChild));

            return leftChildChildExpression.isPresent()
                    ? iqFactory.createUnaryIQTree(iqFactory.createFilterNode(leftChildChildExpression.get()), leftJoinTree)
                    : leftJoinTree;
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<ImmutableExpression> filterChildExpressions = getChildExpressions(children);
            if (filterChildExpressions.isEmpty())
                return tree;

            return iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(getConjunction(
                                            rootNode.getOptionalFilterCondition(),
                                            filterChildExpressions)),
                    children.stream()
                            .map(this::trimRootFilter)
                            .collect(ImmutableCollectors.toList()));
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            ImmutableList<ImmutableExpression> filterChildExpressions = getChildExpressions(ImmutableList.of(child));
            if (filterChildExpressions.isEmpty())
                return tree;

            return iqFactory.createUnaryIQTree(
                    iqFactory.createFilterNode(getConjunction(
                                    Optional.of(rootNode.getFilterCondition()),
                                    filterChildExpressions).get()),
                    trimRootFilter(child));
        }

        private ImmutableList<ImmutableExpression> getChildExpressions(ImmutableList<IQTree> children) {
            return children.stream()
                    .filter(t -> t.getRootNode() instanceof FilterNode)
                    .map(t -> ((FilterNode) t.getRootNode()).getFilterCondition())
                    .collect(ImmutableCollectors.toList());
        }

        private Optional<ImmutableExpression> getOptionalChildExpression(IQTree child) {
            QueryNode root = child.getRootNode();
            return root instanceof FilterNode
                    ? Optional.of(((FilterNode) root).getFilterCondition())
                    : Optional.empty();
        }

        private IQTree trimRootFilter(IQTree tree) {
            return tree.getRootNode() instanceof FilterNode
                    ? ((UnaryIQTree) tree).getChild()
                    : tree;
        }

        protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            return childTransformer.transform(tree);
        }

        protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return childTransformer.transform(tree);
        }

        protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
            return childTransformer.transform(tree);
        }

    }

    private Optional<ImmutableExpression> getConjunction(Optional<ImmutableExpression> optExpression, List<ImmutableExpression> expressions) {
        if (expressions.isEmpty())
            throw new IllegalArgumentException("Nonempty list of filters expected");

        ImmutableExpression result = (optExpression.isPresent()
                    ? Stream.concat(Stream.of(optExpression.get()), expressions.stream())
                    : expressions.stream())
                .reduce(null,
                    (a, b) -> (a == null) ? b : termFactory.getImmutableExpression(AND, a, b));
        return Optional.of(result);
    }
}
