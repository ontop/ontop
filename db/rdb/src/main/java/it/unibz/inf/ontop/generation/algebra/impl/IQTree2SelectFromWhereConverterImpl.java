package it.unibz.inf.ontop.generation.algebra.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.algebra.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.NaryIQTreeDecomposition;

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
                .orElseGet(() -> NaryIQTreeDecomposition.of(childTree, InnerJoinNode.class)
                        .getOptionalNode()
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
        var join = NaryIQTreeDecomposition.of(tree, InnerJoinNode.class);
        if (join.isPresent()) {
            // Removes the joining condition
            InnerJoinNode newInnerJoinNode = join.getNode().changeOptionalFilterCondition(Optional.empty());
            return convertIntoOrdinaryExpression(iqFactory.createNaryIQTree(newInnerJoinNode,
                    join.getChildren()));
        }
        else
            return convertIntoOrdinaryExpression(tree);
    }

    private SQLExpression convertIntoOrdinaryExpression(IQTree tree) {
        return tree.acceptVisitor(new IQVisitor<>() {
            @Override
            public SQLExpression transformIntensionalData(IntensionalDataNode dataNode) {
                throw new MinorOntopInternalBugException("unexpected intensional data node: " + dataNode);
            }

            @Override
            public SQLExpression transformExtensionalData(ExtensionalDataNode extensionalDataNode) {
                return sqlAlgebraFactory.createSQLTable(extensionalDataNode.getRelationDefinition(),
                        extensionalDataNode.getArgumentMap());
            }

            @Override
            public SQLExpression transformEmpty(EmptyNode node) {
                throw new MinorOntopInternalBugException("unexpected empty node"  + node);
            }

            @Override
            public SQLExpression transformTrue(TrueNode node) {
                return sqlAlgebraFactory.createSQLOneTupleDummyQueryExpression();
            }

            @Override
            public SQLExpression transformNative(NativeNode nativeNode) {
                String sqlQuery = nativeNode.getNativeQueryString();
                return sqlAlgebraFactory.createSQLSerializedQuery(sqlQuery, nativeNode.getColumnNames());
            }

            @Override
            public SQLExpression transformValues(ValuesNode valuesNode) {
                return sqlAlgebraFactory.createSQLValues(valuesNode.getOrderedVariables(), valuesNode.getValues());
            }

            @Override
            public SQLExpression transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
                return convert(tree, getSignature(tree));
            }

            @Override
            public SQLExpression transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
                return convert(tree, getSignature(tree));
            }

            @Override
            public SQLExpression transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
                return convert(tree, getSignature(tree));
            }

            @Override
            public SQLExpression transformFlatten(UnaryIQTree tree, FlattenNode flattenNode, IQTree child) {
                IQTree subtree = tree.getChild();
                return sqlAlgebraFactory.createSQLFlattenExpression(
                        convert(subtree, getSignature(subtree)),
                        flattenNode.getFlattenedVariable(),
                        flattenNode.getOutputVariable(),
                        flattenNode.getIndexVariable(),
                        flattenNode.getFlattenedType());
            }

            @Override
            public SQLExpression transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
                return convert(tree, getSignature(tree));
            }

            @Override
            public SQLExpression transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
                return convert(tree, getSignature(tree));
            }

            @Override
            public SQLExpression transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
                return convert(tree, getSignature(tree));
            }

            @Override
            public SQLExpression transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode leftJoinNode, IQTree leftChild, IQTree rightChild) {
                SQLExpression leftExpression = getSubExpressionOfLeftJoinExpression(leftChild);
                SQLExpression rightExpression = getSubExpressionOfLeftJoinExpression(rightChild);

                /*
                 * Where expression: from the filter node or from the top inner join of the child tree
                 */
                Optional<ImmutableExpression> joinCondition = leftJoinNode.getOptionalFilterCondition();

                return sqlAlgebraFactory.createSQLLeftJoinExpression(leftExpression, rightExpression, joinCondition);
            }

            @Override
            public SQLExpression transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
                ImmutableList<SQLExpression> joinedExpressions = tree.getChildren().stream()
                        .map(c -> convertIntoFromExpression(c))
                        .collect(ImmutableCollectors.toList());

                return sqlAlgebraFactory.createSQLNaryJoinExpression(joinedExpressions);
            }

            @Override
            public SQLExpression transformUnion(NaryIQTree tree, UnionNode unionNode, ImmutableList<IQTree> children) {
                ImmutableSortedSet<Variable> signature = getSignature(tree);
                ImmutableList<SQLExpression> subExpressions = tree.getChildren().stream()
                        .map(e-> convert(e, signature))
                        .collect(ImmutableCollectors.toList());
                return sqlAlgebraFactory.createSQLUnionExpression(subExpressions, unionNode.getVariables());
            }

            private ImmutableSortedSet<Variable> getSignature(IQTree tree) {
                return ImmutableSortedSet.copyOf(tree.getVariables());
            }
        });
    }

    private SQLExpression getSubExpressionOfLeftJoinExpression(IQTree tree) {
        var join = NaryIQTreeDecomposition.of(tree, InnerJoinNode.class);
        if (join.isPresent()) {
            ImmutableList<IQTree> children =  join.getChildren();
            int arity = children.size();

            Optional<ImmutableExpression> filterCondition = join.getNode().getOptionalFilterCondition();

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
