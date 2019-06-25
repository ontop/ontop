package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
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
    public IQ optimize(ImmutableCQContainmentCheck cqContainmentCheck, IQ query) {

        IQTree tree = query.getTree().acceptTransformer(new FilterChildNormalizer());

        if (tree.getRootNode() instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode)tree.getRootNode();
            if (tree.getChildren().size() == 1 && tree.getChildren().get(0).getRootNode() instanceof InnerJoinNode) {
                IQTree joinTree = tree.getChildren().get(0);
                InnerJoinNode joinNode = (InnerJoinNode) joinTree.getRootNode();
                ImmutableSet<Variable> answerVariables = Stream.concat(
                        constructionNode.getSubstitution().getImmutableMap().values().stream()
                                .flatMap(t -> t.getVariableStream()),
                        joinNode.getOptionalFilterCondition()
                                .map(f -> f.getVariableStream()).orElse(Stream.of()))
                            .collect(ImmutableCollectors.toSet());
                System.out.println("CQC " + query + " WITH " + answerVariables);
                if (joinTree.getChildren().stream().anyMatch(c -> !(c.getRootNode() instanceof DataNode))) {
                    System.out.println("CQC PANIC - NOT A JOIN OF DATA ATOMS");
                }
                else {
                    if (joinTree.getChildren().size() < 2) {
                        System.out.println("CQC: NOTING TO OPTIMIZE");
                        return query;
                    }

                    List<IQTree> children = new ArrayList<>(joinTree.getChildren());
                    for (int i = 0; i < children.size(); i++) {
                        IQTree toBeRemoved = children.get(i);

                        List<IQTree> toLeave = new ArrayList<>(children.size() - 1);
                        for (IQTree a: children)
                            if (a != toBeRemoved)
                                toLeave.add(a);

                        ImmutableSet<Variable> variablesInToLeave = toLeave.stream().flatMap(a -> a.getVariables().stream()).collect(ImmutableCollectors.toSet());
                        if (!variablesInToLeave.containsAll(answerVariables))
                            continue;

                        System.out.println("CHECK H: " + children + " TO " + toLeave);

                        ImmutableList<Variable> answerVariablesList = ImmutableList.copyOf(answerVariables);
                        ImmutableList<DataAtom<AtomPredicate>> from = children.stream()
                                .map(n -> ((DataNode<AtomPredicate>)n.getRootNode()).getProjectionAtom())
                                .collect(ImmutableCollectors.toList());
                        ImmutableList<DataAtom<AtomPredicate>> to = toLeave.stream()
                                .map(n -> ((DataNode<AtomPredicate>)n.getRootNode()).getProjectionAtom())
                                .collect(ImmutableCollectors.toList());
                        if (cqContainmentCheck.isContainedIn(
                                new ImmutableCQ<>(answerVariablesList, to),
                                new ImmutableCQ<>(answerVariablesList, from))) {
                            System.out.println("CQC: " + to + " IS CONTAINED IN " + from);
                            children.remove(toBeRemoved);
                            i--;
                        }
                    }

                    return iqFactory.createIQ(
                            query.getProjectionAtom(),
                            iqFactory.createUnaryIQTree(
                                (ConstructionNode)tree.getRootNode(),
                                    (children.size() < 2)
                                        ? (joinNode.getOptionalFilterCondition().isPresent()
                                            ? iqFactory.createUnaryIQTree(iqFactory.createFilterNode(joinNode.getOptionalFilterCondition().get()), children.get(0))
                                            : children.get(0))
                                        : iqFactory.createNaryIQTree(joinNode, ImmutableList.copyOf(children))));
                }
            }

        }

        query.getTree().acceptTransformer(new DefaultRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
                System.out.println("CQC Q:" + query + "\nCQC T: " + tree + "\nCQC N:" + rootNode);

                return iqFactory.createNaryIQTree(
                        rootNode,
                        children.stream()
                                .map(t -> t.acceptTransformer(this))
                                .collect(ImmutableCollectors.toList()));
            }
        });
        return query;
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
                                ? iqFactory.createLeftJoinNode(Optional.of(updateJoinCondition(
                                                        rootNode.getOptionalFilterCondition(),
                                                        ImmutableList.of(rightChildExpression.get()))))
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
                    iqFactory.createInnerJoinNode(Optional.of(updateJoinCondition(
                                            rootNode.getOptionalFilterCondition(),
                                            filterChildExpressions))),
                    children.stream()
                            .map(this::trimRootFilter)
                            .collect(ImmutableCollectors.toList())
            );
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            ImmutableList<ImmutableExpression> filterChildExpressions = getChildExpressions(ImmutableList.of(child));
            if (filterChildExpressions.isEmpty())
                return tree;

            return iqFactory.createUnaryIQTree(
                    iqFactory.createFilterNode(updateJoinCondition(
                                    Optional.of(rootNode.getFilterCondition()),
                                    filterChildExpressions)),
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

        private ImmutableExpression updateJoinCondition(Optional<ImmutableExpression> joinCondition, ImmutableList<ImmutableExpression> additionalConditions) {
            if (additionalConditions.isEmpty())
                throw new RuntimeException("Nonempty list of filters expected");
            return getConjunction(joinCondition, additionalConditions.stream());
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

        private ImmutableExpression getConjunction(Stream<ImmutableExpression> expressions) {
            return expressions.reduce(null,
                            (a, b) -> (a == null)
                                    ? b
                                    : termFactory.getImmutableExpression(AND, a, b));
        }

        private ImmutableExpression getConjunction(Optional<ImmutableExpression> optExpression, Stream<ImmutableExpression> expressions) {
            return getConjunction(optExpression.isPresent()
                    ? Stream.concat(Stream.of(optExpression.get()), expressions)
                    : expressions);
        }
    }

}
