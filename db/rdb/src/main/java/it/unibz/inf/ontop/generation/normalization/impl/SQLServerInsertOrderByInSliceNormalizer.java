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
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
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
        if(child instanceof OrderByNode)
            return true;
        if(child instanceof DistinctNode) {
            var grandchild = child.getChildren().get(0);
            if(grandchild instanceof OrderByNode)
                return true;
        }
        return false;
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
}
