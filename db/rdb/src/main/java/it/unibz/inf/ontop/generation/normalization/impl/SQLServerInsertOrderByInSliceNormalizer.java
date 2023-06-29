package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class SQLServerInsertOrderByInSliceNormalizer extends DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator>
        implements DialectExtraNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    protected SQLServerInsertOrderByInSliceNormalizer(IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                                      SubstitutionFactory substitutionFactory, CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator context) {
        return super.transform(tree, context);
    }

    private boolean isOrderByPresent(IQTree child) {
        return child.acceptVisitor(new OrderBySearcher());
    }

    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child, VariableGenerator context) {
        if(isOrderByPresent(child)) {
            return iqFactory.createUnaryIQTree(
                    sliceNode,
                    transform(child, context)
            );
        }
        var topConstruct = iqFactory.createConstructionNode(tree.getVariables(), substitutionFactory.getSubstitution());
        var sortVariable = context.generateNewVariable("slice_sort_column");
        var bottomConstruct = iqFactory.createConstructionNode(
                Sets.union(tree.getVariables(), ImmutableSet.of(sortVariable)).immutableCopy(),
                substitutionFactory.getSubstitution(
                        sortVariable,
                        termFactory.getDBConstant("", termFactory.getTypeFactory().getDBTypeFactory().getDBStringType())));
        var orderByNode = iqFactory.createOrderByNode(ImmutableList.of(iqFactory.createOrderComparator(sortVariable, true)));

        return iqFactory.createUnaryIQTree(
                sliceNode,
                iqFactory.createUnaryIQTree(
                        topConstruct,
                        iqFactory.createUnaryIQTree(
                                orderByNode,
                                iqFactory.createUnaryIQTree(
                                        bottomConstruct,
                                        transform(child, context)
                                )
                        )
                )
        );
    }

    /**
     * Search for an ORDER BY clause in the child IQTree. Keep searching until a node is found that will cause a new sub-query.
     */
    static class OrderBySearcher implements IQVisitor<Boolean> {

        @Override
        public Boolean visitIntensionalData(IntensionalDataNode dataNode) {
            return false;
        }

        @Override
        public Boolean visitExtensionalData(ExtensionalDataNode dataNode) {
            return false;
        }

        @Override
        public Boolean visitEmpty(EmptyNode node) {
            return false;
        }

        @Override
        public Boolean visitTrue(TrueNode node) {
            return false;
        }

        @Override
        public Boolean visitNative(NativeNode nativeNode) {
            return false;
        }

        @Override
        public Boolean visitValues(ValuesNode valuesNode) {
            return false;
        }

        @Override
        public Boolean visitNonStandardLeafNode(LeafIQTree leafNode) {
            return false;
        }

        @Override
        public Boolean visitConstruction(ConstructionNode rootNode, IQTree child) {
            return false;
        }

        @Override
        public Boolean visitAggregation(AggregationNode aggregationNode, IQTree child) {
            return false;
        }

        @Override
        public Boolean visitFilter(FilterNode rootNode, IQTree child) {
            return child.acceptVisitor(this);
        }

        @Override
        public Boolean visitFlatten(FlattenNode rootNode, IQTree child) {
            return false;
        }

        @Override
        public Boolean visitDistinct(DistinctNode rootNode, IQTree child) {
            return child.acceptVisitor(this);
        }

        @Override
        public Boolean visitSlice(SliceNode sliceNode, IQTree child) {
            return false;
        }

        @Override
        public Boolean visitOrderBy(OrderByNode rootNode, IQTree child) {
            return true;
        }

        @Override
        public Boolean visitNonStandardUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
            return false;
        }

        @Override
        public Boolean visitLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            return false;
        }

        @Override
        public Boolean visitNonStandardBinaryNonCommutativeNode(BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
            return false;
        }

        @Override
        public Boolean visitInnerJoin(InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return false;
        }

        @Override
        public Boolean visitUnion(UnionNode rootNode, ImmutableList<IQTree> children) {
            return false;
        }

        @Override
        public Boolean visitNonStandardNaryNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return false;
        }
    }
}
