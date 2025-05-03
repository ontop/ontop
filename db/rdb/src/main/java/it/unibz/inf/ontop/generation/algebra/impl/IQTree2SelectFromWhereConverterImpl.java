package it.unibz.inf.ontop.generation.algebra.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.algebra.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

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

        var slice = UnaryIQTreeDecomposition.of(tree, SliceNode.class);
        Optional<SliceNode> sliceNode = slice.getOptionalNode();

        var distinct = UnaryIQTreeDecomposition.of(slice.getChild(), DistinctNode.class);
        Optional<DistinctNode> distinctNode = distinct.getOptionalNode();

        var construction = UnaryIQTreeDecomposition.of(distinct.getChild(), ConstructionNode.class);
        Optional<ConstructionNode> constructionNode = construction.getOptionalNode();

        var orderBy = UnaryIQTreeDecomposition.of(construction.getChild(), OrderByNode.class);
        Optional<OrderByNode> orderByNode = orderBy.getOptionalNode();

        var aggregation = UnaryIQTreeDecomposition.of(orderBy.getChild(), AggregationNode.class);
        Optional<AggregationNode> aggregationNode = aggregation.getOptionalNode();

        var filter = UnaryIQTreeDecomposition.of(aggregation.getChild(), FilterNode.class);
        Optional<FilterNode> filterNode = filter.getOptionalNode();

        IQTree childTree = filter.getChild();

        Substitution<ImmutableTerm> substitution = constructionNode
                .map(c -> aggregationNode
                        .map(AggregationNode::getSubstitution)
                        .map(s2 -> s2.compose(c.getSubstitution())
                                .restrictDomainTo(c.getVariables()))
                        .orElseGet(c::getSubstitution))
                .orElseGet(() -> aggregationNode
                        .map(AggregationNode::getSubstitution)
                        .map(substitutionFactory::<ImmutableTerm>covariantCast)
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
                                        substitutionFactory.onNonConstantTerms().applyToTerm(s, c.getTerm()),
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
            ImmutableList<SQLExpression> joinedExpressions = tree.getChildren().stream()
                    .map(this::convertIntoFromExpression)
                    .collect(ImmutableCollectors.toList());

            return sqlAlgebraFactory.createSQLNaryJoinExpression(joinedExpressions);
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
        else if (rootNode instanceof FlattenNode) {
            FlattenNode flattenNode = (FlattenNode) rootNode;
            IQTree subtree = tree.getChildren().get(0);
            return sqlAlgebraFactory.createSQLFlattenExpression(
                    convert(
                            subtree,
                            ImmutableSortedSet.copyOf(subtree.getVariables())
                    ),
                    flattenNode.getFlattenedVariable(),
                    flattenNode.getOutputVariable(),
                    flattenNode.getIndexVariable(),
                    flattenNode.getFlattenedType()
            );
        }
        else
            throw new RuntimeException("TODO: support arbitrary relations");
    }

    private SQLExpression getSubExpressionOfLeftJoinExpression(IQTree tree){
        if (tree.getRootNode() instanceof InnerJoinNode) {
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
