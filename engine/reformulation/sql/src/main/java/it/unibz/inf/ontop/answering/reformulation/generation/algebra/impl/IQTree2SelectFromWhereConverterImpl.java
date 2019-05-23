package it.unibz.inf.ontop.answering.reformulation.generation.algebra.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.IQTree2SelectFromWhereConverter;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLAlgebraFactory;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLExpression;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import java.util.Optional;

public class IQTree2SelectFromWhereConverterImpl implements IQTree2SelectFromWhereConverter {

    private final SQLAlgebraFactory sqlAlgebraFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private IQTree2SelectFromWhereConverterImpl(SQLAlgebraFactory sqlAlgebraFactory, SubstitutionFactory substitutionFactory) {
        this.sqlAlgebraFactory = sqlAlgebraFactory;
        this.substitutionFactory = substitutionFactory;
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

        Optional<FilterNode> filterNode = Optional.of(firstNonSliceDistinctConstructionOrderByTree)
                .map(IQTree::getRootNode)
                .filter(n -> n instanceof FilterNode)
                .map(n -> (FilterNode) n);

        IQTree childTree = filterNode
                .map(n -> ((UnaryIQTree) firstNonSliceDistinctConstructionOrderByTree).getChild())
                .orElse(firstNonSliceDistinctConstructionOrderByTree);

        ImmutableSubstitution<ImmutableTerm> substitution = constructionNode
                .map(ConstructionNode::getSubstitution)
                .orElseGet(substitutionFactory::getSubstitution);

        ImmutableList<? extends SQLExpression> fromRelations = convertIntoFromRelations(
                firstNonSliceDistinctConstructionOrderByTree);

        /*
         * Where expression: from the filter node or from the top inner join of the child tree
         */
        Optional<ImmutableExpression> whereExpression = filterNode
                .map(JoinOrFilterNode::getOptionalFilterCondition)
                .orElseGet(() -> Optional.of(childTree.getRootNode())
                        .filter(n -> n instanceof InnerJoinNode)
                        .map(n -> (InnerJoinNode) n)
                        .flatMap(JoinOrFilterNode::getOptionalFilterCondition));

        return sqlAlgebraFactory.createSelectFromWhere(signature, substitution, fromRelations, whereExpression,
                distinctNode.isPresent(),
                sliceNode
                        .flatMap(SliceNode::getLimit),
                sliceNode
                        .map(SliceNode::getOffset)
                        .filter(o -> o > 0),
                orderByNode
                        .map(OrderByNode::getComparators)
                        .orElseGet(ImmutableList::of));
    }

    /**
     * TODO: implement it seriously
     *
     * Ignores the top filtering expression of the root node if the latter is an inner join,
     * as this expression will be used as WHERE expression instead.
     *
     */
    private ImmutableList<? extends SQLExpression> convertIntoFromRelations(IQTree tree) {
        QueryNode rootNode = tree.getRootNode();
        if (rootNode instanceof NativeNode) {
            NativeNode nativeNode = (NativeNode) rootNode;
            String sqlQuery = nativeNode.getNativeQueryString();
            return ImmutableList.of(sqlAlgebraFactory.createSQLSerializedQuery(sqlQuery, nativeNode.getColumnNames()));
        }
        else if (rootNode instanceof  ExtensionalDataNode){
            ExtensionalDataNode extensionalDataNode = (ExtensionalDataNode) rootNode;

            return null;
        }
        else
            throw new RuntimeException("TODO: support arbitrary relations");
    }
}
