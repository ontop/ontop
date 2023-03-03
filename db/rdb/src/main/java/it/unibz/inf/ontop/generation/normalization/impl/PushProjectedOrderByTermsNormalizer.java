package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.node.impl.OrderComparatorImpl;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Collectors;

/*
Used when an ORDER BY node accesses variables that are defined in a CONSTRUCT above it. Some dialects (like GoogleSQL) do
not support that, so instead we push the CONSTRUCT down into the ORDER BY.
 */
public class PushProjectedOrderByTermsNormalizer extends DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator> implements DialectExtraNormalizer {

    private AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer;
    private IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    protected PushProjectedOrderByTermsNormalizer(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                                                  AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer, CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.alwaysProjectOrderByTermsNormalizer = alwaysProjectOrderByTermsNormalizer;
    }


    @Override
    public IQTree transform(IQTree tree, VariableGenerator context) {

        //Run `AlwaysProjectOrderByTermsNormalizer` to prepare transformation.
        IQTree projectedOrderByTerms = alwaysProjectOrderByTermsNormalizer.transform(tree, context);

        //We only change trees of the form DISTINCT -> CONSTRUCT -> ORDER BY. Make sure our tree has that exact form,
        //else we return the results of the `AlwaysProjectOrderByTermsNormalizer`
        if(!(projectedOrderByTerms.getRootNode() instanceof DistinctNode) || projectedOrderByTerms.getChildren().size() == 0)
            return projectedOrderByTerms;
        DistinctNode distinct = (DistinctNode) projectedOrderByTerms.getRootNode();
        var distinctSubtree = projectedOrderByTerms.getChildren().get(0);

        if(!(distinctSubtree.getRootNode() instanceof ConstructionNode) || distinctSubtree.getChildren().size() == 0)
            return projectedOrderByTerms;
        ConstructionNode construct = (ConstructionNode) distinctSubtree.getRootNode();
        var constructSubtree = distinctSubtree.getChildren().get(0);

        if(!(constructSubtree.getRootNode() instanceof OrderByNode) || constructSubtree.getChildren().size() == 0)
            return projectedOrderByTerms;
        OrderByNode orderBy = (OrderByNode) constructSubtree.getRootNode();
        var orderBySubtree = constructSubtree.getChildren().get(0);

        //DISTINCT, CONSTRUCT, and ORDER BY nodes + their subtrees have been fetched

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
        var newOrderBySubtree = iqFactory.createUnaryIQTree(construct, orderBySubtree);
        var newDistinctSubtree = iqFactory.createUnaryIQTree(newOrderBy, newOrderBySubtree);
        var newFullTree = iqFactory.createUnaryIQTree(distinct, newDistinctSubtree);

        return newFullTree;
    }
}
