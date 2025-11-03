package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.IQTreeVisitingNodeTransformer;
import it.unibz.inf.ontop.iq.type.TermTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public abstract class AbstractTermTransformer implements TermTransformer {
    protected final IntermediateQueryFactory iqFactory;
    protected final TermFactory termFactory;

    protected AbstractTermTransformer(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
    }

    public IQTreeTransformer treeTransformer() {
        var treeTransformer = new IQTreeVisitingNodeTransformer(
                new QueryNodeTransformerAdapter(iqFactory, this), iqFactory);
        return t -> t.acceptVisitor(treeTransformer);
    }

    @Override
    public ImmutableTerm transformTerm(ImmutableTerm term, IQTree tree) {
        return (term instanceof ImmutableFunctionalTerm)
                ? transformFunctionalTerm((ImmutableFunctionalTerm)term, tree)
                : term;
    }

    @Override
    public NonGroundTerm transformNonGroundTerm(NonGroundTerm term, IQTree tree) {
        return (NonGroundTerm) transformTerm(term, tree);
    }

    @Override
    public ImmutableExpression transformExpression(ImmutableExpression expression, IQTree tree) {
        return (ImmutableExpression) transformFunctionalTerm(expression, tree);
    }

    /**
     * Recursive
     */
    @Override
    public ImmutableFunctionalTerm transformFunctionalTerm(ImmutableFunctionalTerm functionalTerm, IQTree tree) {
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
