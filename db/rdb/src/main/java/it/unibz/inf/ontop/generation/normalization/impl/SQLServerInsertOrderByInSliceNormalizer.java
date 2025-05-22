package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class SQLServerInsertOrderByInSliceNormalizer implements DialectExtraNormalizer {

    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected SQLServerInsertOrderByInSliceNormalizer(CoreSingletons coreSingletons) {
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(new Transformer(variableGenerator));
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final VariableGenerator variableGenerator;

        protected Transformer(VariableGenerator variableGenerator) {
            super(SQLServerInsertOrderByInSliceNormalizer.this.iqFactory);
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
            if (IQTreeTools.contains(child, OrderByNode.class)) {
                return iqFactory.createUnaryIQTree(
                        sliceNode,
                        transform(child));
            }
            var topConstruct = iqFactory.createConstructionNode(tree.getVariables());
            var sortVariable = variableGenerator.generateNewVariable("slice_sort_column");
            var bottomConstruct = iqTreeTools.createExtendingConstructionNode(
                    tree.getVariables(),
                    substitutionFactory.getSubstitution(
                            sortVariable,
                            termFactory.getDBConstant("", termFactory.getTypeFactory().getDBTypeFactory().getDBStringType())));
            var orderByNode = iqFactory.createOrderByNode(ImmutableList.of(iqFactory.createOrderComparator(sortVariable, true)));

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(sliceNode)
                    .append(topConstruct)
                    .append(orderByNode)
                    .append(bottomConstruct)
                    .build(transform(child));
        }
    }
}
