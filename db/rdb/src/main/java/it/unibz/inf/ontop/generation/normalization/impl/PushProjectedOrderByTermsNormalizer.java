package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

/*
Used when an ORDER BY node accesses expressions that are defined in a CONSTRUCT above it. Some dialects (like GoogleSQL) do
not support that, so instead we push the CONSTRUCT down into the ORDER BY so the ORDER BY can use the variables defined by
the CONSTRUCT.
Generally, an `AlwaysProjectOrderByTerms` normalizer is expected to be run before calling this normalizer.
 */
public class PushProjectedOrderByTermsNormalizer extends DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator> implements DialectExtraNormalizer {

    private IntermediateQueryFactory iqFactory;

    @Inject
    protected PushProjectedOrderByTermsNormalizer(IntermediateQueryFactory iqFactory,
                                                  CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.iqFactory = iqFactory;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptTransformer(this, variableGenerator);
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child, VariableGenerator variableGenerator) {
        var decomposition = ProjectOrderByTermsNormalizer.Decomposition.decomposeTree(tree);
        if(decomposition.constructionNode.isEmpty() || decomposition.distinctNode.isEmpty() || decomposition.orderByNode.isEmpty()) {
            var newDescendantTree = transform(decomposition.descendantTree, variableGenerator);
            return decomposition.descendantTree.equals(newDescendantTree)
                    ? tree
                    : decomposition.rebuildWithNewDescendantTree(newDescendantTree, iqFactory);
        }
        return normalize(decomposition, variableGenerator);
    }

    protected IQTree normalize(ProjectOrderByTermsNormalizer.Decomposition decomposition, VariableGenerator context) {
        var distinct = decomposition.distinctNode.get();
        var construct = decomposition.constructionNode.get();
        var orderBy = decomposition.orderByNode.get();
        var remainingSubtree = transform(decomposition.descendantTree, context);

        //Get map of terms used in ORDER BY that are defined in CONSTRUCT
        var orderByTerms = orderBy.getComparators().stream().map(comp -> comp.getTerm()).collect(ImmutableCollectors.toSet());
        var definedInConstruct = orderByTerms.stream().filter(
                term -> construct.getSubstitution().getImmutableMap().values().stream().anyMatch(t -> t.equals(term))
        ).collect(ImmutableCollectors.toMap(
                term -> term,
                term -> (NonGroundTerm) construct.getSubstitution().getImmutableMap().keySet().stream().filter(t -> construct.getSubstitution().get(t).equals(term)).findFirst().get()
        ));

        //Define new ORDER BY node that uses variables from CONSTRUCT instead, where possible
        var newOrderBy = iqFactory.createOrderByNode(orderBy.getComparators().stream().map(
                comp -> iqFactory.createOrderComparator(definedInConstruct.getOrDefault(comp.getTerm(), comp.getTerm()), comp.isAscending())
        ).collect(ImmutableCollectors.toList()));

        //Change order from DISTINCT -> CONSTRUCT -> ORDER BY to DISTINCT -> ORDER BY -> CONSTRUCT
        var newOrderBySubtree = iqFactory.createUnaryIQTree(construct, remainingSubtree);
        var newDistinctSubtree = iqFactory.createUnaryIQTree(newOrderBy, newOrderBySubtree);
        var newFullTree = iqFactory.createUnaryIQTree(distinct, newDistinctSubtree);
        return newFullTree;
    }
}
