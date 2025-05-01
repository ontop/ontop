package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class SQLServerInsertOrderByInSliceNormalizer implements DialectExtraNormalizer {

    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected SQLServerInsertOrderByInSliceNormalizer(CoreSingletons coreSingletons) {
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptTransformer(new Transformer(variableGenerator));
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final VariableGenerator variableGenerator;

        protected Transformer(VariableGenerator variableGenerator) {
            super(SQLServerInsertOrderByInSliceNormalizer.this.iqFactory);
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
            if (isOrderByPresent(child)) {
                return iqFactory.createUnaryIQTree(
                        sliceNode,
                        transform(child));
            }
            var topConstruct = iqFactory.createConstructionNode(tree.getVariables(), substitutionFactory.getSubstitution());
            var sortVariable = variableGenerator.generateNewVariable("slice_sort_column");
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
                                            transform(child)))));
        }

        private boolean isOrderByPresent(IQTree child) {
            return child.acceptVisitor(new OrderBySearcher());
        }
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
        public Boolean visitConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            return false;
        }

        @Override
        public Boolean visitAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child) {
            return false;
        }

        @Override
        public Boolean visitFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            return child.acceptVisitor(this);
        }

        @Override
        public Boolean visitFlatten(IQTree tree, FlattenNode rootNode, IQTree child) {
            return false;
        }

        @Override
        public Boolean visitDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
            return child.acceptVisitor(this);
        }

        @Override
        public Boolean visitSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
            return false;
        }

        @Override
        public Boolean visitOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
            return true;
        }

        @Override
        public Boolean visitLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            return false;
        }

        @Override
        public Boolean visitInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return false;
        }

        @Override
        public Boolean visitUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            return false;
        }
    }
}
