package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.IQTreeTransformerAdapter;
import it.unibz.inf.ontop.iq.transform.impl.IQTreeVisitingNodeTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public abstract class AbstractTermTransformer {
    protected final TermFactory termFactory;

    protected AbstractTermTransformer(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    public IQTreeTransformer treeTransformer(IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools) {
        return new IQTreeTransformerAdapter(
                new IQTreeVisitingNodeTransformer(
                        new QueryNodeTransformerAdapter(iqFactory, iqTreeTools, this), iqFactory));
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
