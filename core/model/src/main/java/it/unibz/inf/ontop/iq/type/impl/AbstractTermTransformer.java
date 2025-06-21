package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.node.DefaultQueryNodeTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;


public abstract class AbstractTermTransformer extends DefaultQueryNodeTransformer {

    protected final SingleTermTypeExtractor typeExtractor;
    protected final TermFactory termFactory;
    protected final IQTreeTools iqTreeTools;

    // this constructor is needed because some uses are in the "parts" of CoreSingletons,
    // which would introduce a cyclic dependency for Guice
    protected AbstractTermTransformer(IntermediateQueryFactory iqFactory,
                                      SingleTermTypeExtractor typeExtractor,
                                      TermFactory termFactory, IQTreeTools iqTreeTools) {
        super(iqFactory);
        this.typeExtractor = typeExtractor;
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
    }

    protected AbstractTermTransformer(CoreSingletons coreSingletons) {
        this(coreSingletons.getIQFactory(),
                coreSingletons.getUniqueTermTypeExtractor(),
                coreSingletons.getTermFactory(),
                coreSingletons.getIQTreeTools());
    }

    @Override
    public ConstructionNode transform(ConstructionNode rootNode, UnaryIQTree tree) {
        return iqTreeTools.replaceSubstitution(rootNode,
                s -> s.transform(t -> transformTerm(t, tree.getChild())));
    }

    @Override
    public AggregationNode transform(AggregationNode rootNode, UnaryIQTree tree) {
        return iqFactory.createAggregationNode(
                rootNode.getGroupingVariables(),
                rootNode.getSubstitution()
                        .transform(t -> transformFunctionalTerm(t, tree.getChild())));
    }

    @Override
    public FilterNode transform(FilterNode rootNode, UnaryIQTree tree) {
        return iqFactory.createFilterNode(
                transformExpression(rootNode.getFilterCondition(), tree));
    }

    @Override
    public OrderByNode transform(OrderByNode rootNode, UnaryIQTree tree) {
        return iqFactory.createOrderByNode(rootNode.getComparators().stream()
                .map(c -> iqFactory.createOrderComparator(
                        transformNonGroundTerm(c.getTerm(), tree),
                        c.isAscending()))
                .collect(ImmutableCollectors.toList()));
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode rootNode, BinaryNonCommutativeIQTree tree) {
        return iqFactory.createLeftJoinNode(rootNode.getOptionalFilterCondition()
                        .map(e -> transformExpression(e, tree)));
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode rootNode, NaryIQTree tree) {
        return iqFactory.createInnerJoinNode(
                rootNode.getOptionalFilterCondition()
                .map(e -> transformExpression(e, tree)));
    }

    protected ImmutableTerm transformTerm(ImmutableTerm term, IQTree tree) {
        return (term instanceof ImmutableFunctionalTerm)
                ? transformFunctionalTerm((ImmutableFunctionalTerm)term, tree)
                : term;
    }

    protected NonGroundTerm transformNonGroundTerm(NonGroundTerm term, IQTree tree) {
        return (term instanceof ImmutableFunctionalTerm)
                ? (NonGroundTerm) transformFunctionalTerm((ImmutableFunctionalTerm)term, tree)
                : term;
    }

    protected ImmutableExpression transformExpression(ImmutableExpression expression, IQTree tree) {
        return (ImmutableExpression) transformFunctionalTerm(expression, tree);
    }

    /**
     * Recursive
     */
    protected ImmutableFunctionalTerm transformFunctionalTerm(ImmutableFunctionalTerm functionalTerm, IQTree tree) {
        ImmutableList<? extends ImmutableTerm> initialTerms = functionalTerm.getTerms();
        ImmutableList<ImmutableTerm> newTerms = initialTerms.stream()
                // Recursive
                .map(t -> transformTerm(t, tree))
                .collect(ImmutableCollectors.toList());

        FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

        var optionalFunctionalTerm = replaceFunctionSymbol(functionSymbol, newTerms, tree);
        return optionalFunctionalTerm
                .orElseGet(() -> newTerms.equals(initialTerms)
                    ? functionalTerm
                    : termFactory.getImmutableFunctionalTerm(functionSymbol, newTerms));
    }

    protected abstract Optional<ImmutableFunctionalTerm> replaceFunctionSymbol(FunctionSymbol functionSymbol, ImmutableList<ImmutableTerm> newTerms, IQTree tree);
}
