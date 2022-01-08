package it.unibz.inf.ontop.generation.algebra.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.algebra.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IQTree2SelectFromWhereConverterImpl implements IQTree2SelectFromWhereConverter {

    private final SQLAlgebraFactory sqlAlgebraFactory;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private IQTree2SelectFromWhereConverterImpl(SQLAlgebraFactory sqlAlgebraFactory,
                                                SubstitutionFactory substitutionFactory,
                                                IntermediateQueryFactory iqFactory) {
        this.sqlAlgebraFactory = sqlAlgebraFactory;
        this.substitutionFactory = substitutionFactory;
        this.iqFactory = iqFactory;
    }

    @Override
    public SelectFromWhereWithModifiers convert(IQTree tree, ImmutableSortedSet<Variable> signature) {

        QueryNode rootNode = tree.getRootNode();
        Optional<SliceNode> sliceNode = Optional.of(rootNode)
                .filter(n -> n instanceof SliceNode)
                .map(n -> (SliceNode) n);

        IQTree firstNonSliceTree = sliceNode
                .map(n -> ((UnaryIQTree) tree).getChild())
                .orElse(tree);

        Optional<DistinctNode> distinctNode = Optional.of(firstNonSliceTree)
                .map(IQTree::getRootNode)
                .filter(n -> n instanceof DistinctNode)
                .map(n -> (DistinctNode) n);

        IQTree firstNonSliceDistinctTree = distinctNode
                .map(n -> ((UnaryIQTree) firstNonSliceTree).getChild())
                .orElse(firstNonSliceTree);

        Optional<ConstructionNode> constructionNode = Optional.of(firstNonSliceDistinctTree)
                .map(IQTree::getRootNode)
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n);

        IQTree firstNonSliceDistinctConstructionTree = constructionNode
                .map(n -> ((UnaryIQTree) firstNonSliceDistinctTree).getChild())
                .orElse(firstNonSliceDistinctTree);

        Optional<OrderByNode> orderByNode = Optional.of(firstNonSliceDistinctConstructionTree)
                .map(IQTree::getRootNode)
                .filter(n -> n instanceof OrderByNode)
                .map(n -> (OrderByNode) n);

        IQTree firstNonSliceDistinctConstructionOrderByTree = orderByNode
                .map(n -> ((UnaryIQTree) firstNonSliceDistinctConstructionTree).getChild())
                .orElse(firstNonSliceDistinctConstructionTree);

        Optional<AggregationNode> aggregationNode = Optional.of(firstNonSliceDistinctConstructionOrderByTree)
                .map(IQTree::getRootNode)
                .filter(n -> n instanceof AggregationNode)
                .map(n -> (AggregationNode) n);

        IQTree firstNonSliceDistinctConstructionOrderByAggregationTree = aggregationNode
                .map(n -> ((UnaryIQTree) firstNonSliceDistinctConstructionOrderByTree).getChild())
                .orElse(firstNonSliceDistinctConstructionOrderByTree);

        Optional<FilterNode> filterNode = Optional.of(firstNonSliceDistinctConstructionOrderByAggregationTree)
                .map(IQTree::getRootNode)
                .filter(n -> n instanceof FilterNode)
                .map(n -> (FilterNode) n);

        IQTree childTree = filterNode
                .map(n -> ((UnaryIQTree) firstNonSliceDistinctConstructionOrderByAggregationTree).getChild())
                .orElse(firstNonSliceDistinctConstructionOrderByAggregationTree);

        ImmutableSubstitution<ImmutableTerm> substitution = constructionNode
                .map(c -> aggregationNode
                        .map(AggregationNode::getSubstitution)
                        .map(s2 -> s2.composeWith(c.getSubstitution()).filter(c.getVariables()::contains))
                        .orElseGet(c::getSubstitution))
                .orElseGet(() -> aggregationNode
                        .map(AggregationNode::getSubstitution)
                        .map(s -> (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)s)
                        .orElseGet(substitutionFactory::getSubstitution));

        SQLExpression fromExpression = convertIntoFromExpression(childTree);

        /*
         * Where expression: from the filter node or from the top inner join of the child tree
         */
        Optional<ImmutableExpression> whereExpression = filterNode
                .map(JoinOrFilterNode::getOptionalFilterCondition)
                .orElseGet(() -> Optional.of(childTree.getRootNode())
                        .filter(n -> n instanceof InnerJoinNode)
                        .map(n -> (InnerJoinNode) n)
                        .flatMap(JoinOrFilterNode::getOptionalFilterCondition));

        ImmutableList<SQLOrderComparator> comparators = extractComparators(orderByNode, aggregationNode);

        return sqlAlgebraFactory.createSelectFromWhere(signature, substitution, fromExpression, whereExpression,
                aggregationNode
                        .map(AggregationNode::getGroupingVariables)
                        .orElseGet(ImmutableSet::of),
                distinctNode.isPresent(),
                sliceNode
                        .flatMap(SliceNode::getLimit),
                sliceNode
                        .map(SliceNode::getOffset)
                        .filter(o -> o > 0),
                comparators);
    }

    private ImmutableList<SQLOrderComparator> extractComparators(Optional<OrderByNode> orderByNode,
                                                                 Optional<AggregationNode> aggregationNode) {
        return orderByNode
                .map(OrderByNode::getComparators)
                .map(cs -> aggregationNode
                        .map(AggregationNode::getSubstitution)
                        .map(s -> cs.stream()
                                .map(c -> sqlAlgebraFactory.createSQLOrderComparator(
                                        (NonConstantTerm) s.apply(c.getTerm()),
                                        c.isAscending()))
                                .collect(ImmutableCollectors.toList()))
                        .orElseGet(() -> cs.stream()
                                .map(c -> sqlAlgebraFactory.createSQLOrderComparator(c.getTerm(), c.isAscending()))
                                .collect(ImmutableCollectors.toList())))
                .orElseGet(ImmutableList::of);
    }

    /**
     *
     * Ignores the top filtering expression of the root node if the latter is an inner join,
     * as this expression will be used as WHERE expression instead.
     *
     */
    private SQLExpression convertIntoFromExpression(IQTree tree) {
        QueryNode rootNode = tree.getRootNode();
        if (rootNode instanceof InnerJoinNode) {
            InnerJoinNode innerJoinNode = (InnerJoinNode) rootNode;
            // Removes the joining condition
            InnerJoinNode newInnerJoinNode = innerJoinNode.changeOptionalFilterCondition(Optional.empty());

            return convertIntoOrdinaryExpression(iqFactory.createNaryIQTree(newInnerJoinNode,
                    tree.getChildren()));
        }
        else
            return convertIntoOrdinaryExpression(tree);
    }

    /**
     * TODO: use an IQVisitor
     */
    private SQLExpression convertIntoOrdinaryExpression(IQTree tree) {
        QueryNode rootNode = tree.getRootNode();
        if (rootNode instanceof NativeNode) {
            NativeNode nativeNode = (NativeNode) rootNode;
            String sqlQuery = nativeNode.getNativeQueryString();
            return sqlAlgebraFactory.createSQLSerializedQuery(sqlQuery, nativeNode.getColumnNames());
        }
        else if (rootNode instanceof  ExtensionalDataNode){
            ExtensionalDataNode extensionalDataNode = (ExtensionalDataNode) rootNode;

            return sqlAlgebraFactory.createSQLTable(extensionalDataNode.getRelationDefinition(),
                    extensionalDataNode.getArgumentMap());
        }
        else if (rootNode instanceof InnerJoinNode){
            List<SQLExpression> joinedExpressions = tree.getChildren().stream()
                    .map(this::convertIntoFromExpression)
                    .collect(Collectors.toList());

            return sqlAlgebraFactory.createSQLNaryJoinExpression(ImmutableList.copyOf(joinedExpressions));
        }
        else if (rootNode instanceof LeftJoinNode){
            LeftJoinNode leftJoinNode = (LeftJoinNode) rootNode;
            IQTree leftSubTree = tree.getChildren().get(0);
            IQTree rightSubTree = tree.getChildren().get(1);

            SQLExpression leftExpression = getSubExpressionOfLeftJoinExpression(leftSubTree);
            SQLExpression rightExpression = getSubExpressionOfLeftJoinExpression(rightSubTree);

            /*
             * Where expression: from the filter node or from the top inner join of the child tree
             */
            Optional<ImmutableExpression> joinCondition = leftJoinNode.getOptionalFilterCondition();

            return sqlAlgebraFactory.createSQLLeftJoinExpression(leftExpression, rightExpression, joinCondition);
        }
        else if (rootNode instanceof UnionNode){
            UnionNode unionNode = (UnionNode) rootNode;
            ImmutableSortedSet<Variable> signature = ImmutableSortedSet.copyOf(tree.getVariables());
            ImmutableList<SQLExpression> subExpressions = tree.getChildren().stream()
                    .map(e-> convert(e, signature))
                    .collect(ImmutableCollectors.toList());
            return sqlAlgebraFactory.createSQLUnionExpression(subExpressions,unionNode.getVariables());
        }
        else if (rootNode instanceof TrueNode){
            return sqlAlgebraFactory.createSQLOneTupleDummyQueryExpression();
        }
        else if (rootNode instanceof ExtendedProjectionNode || rootNode instanceof QueryModifierNode){
            ImmutableSortedSet<Variable> signature = ImmutableSortedSet.copyOf(tree.getVariables());
            return convert(tree, signature);
        }
        else if (rootNode instanceof ValuesNode) {
            ValuesNode valuesNode = (ValuesNode) rootNode;
            return sqlAlgebraFactory.createSQLValues(valuesNode.getOrderedVariables(), valuesNode.getValues());
        }
        else
            throw new RuntimeException("TODO: support arbitrary relations");
    }

    private SQLExpression getSubExpressionOfLeftJoinExpression(IQTree tree){
        if (tree.getRootNode() instanceof InnerJoinNode){
            ImmutableList<IQTree> children = tree.getChildren();
            int arity = children.size();

            Optional<ImmutableExpression> filterCondition = ((InnerJoinNode) tree.getRootNode()).getOptionalFilterCondition();

            return IntStream.range(1, arity)
                    .boxed()
                    .reduce(convertIntoOrdinaryExpression(children.get(0)),
                            (e, i) -> sqlAlgebraFactory.createSQLInnerJoinExpression(
                                    e,
                                    convertIntoOrdinaryExpression(children.get(i)),
                                    filterCondition
                                            // We only consider the joining condition when reaching the ultimate child
                                            .filter(c -> i == (arity - 1))),
                            (e1, e2) -> { throw new MinorOntopInternalBugException("Unexpected");});
        }
        return convertIntoOrdinaryExpression(tree);
    }
}
