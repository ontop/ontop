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
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class SQLServerInsertOrderByInSliceNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected SQLServerInsertOrderByInSliceNormalizer(CoreSingletons coreSingletons) {
        super(IQTreeVariableGeneratorTransformer.of(
                vg -> new Transformer(vg, coreSingletons)));
    }

    private static class Transformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {
        private final SubstitutionFactory substitutionFactory;
        private final TermFactory termFactory;
        private final IQTreeTools iqTreeTools;

        Transformer(VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
            super(coreSingletons.getIQFactory(), variableGenerator);
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
            this.termFactory = coreSingletons.getTermFactory();
            this.iqTreeTools = coreSingletons.getIQTreeTools();
        }

        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
            if (IQTreeTools.contains(child, OrderByNode.class)) {
                return super.transformSlice(tree, sliceNode, child);
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
