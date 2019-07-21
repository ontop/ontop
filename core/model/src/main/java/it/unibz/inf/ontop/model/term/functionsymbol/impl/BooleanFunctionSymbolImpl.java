package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class BooleanFunctionSymbolImpl extends FunctionSymbolImpl implements BooleanFunctionSymbol {

    private final DBTermType dbBooleanTermType;

    protected BooleanFunctionSymbolImpl(@Nonnull String name, @Nonnull ImmutableList<TermType> expectedBaseTypes,
                                        DBTermType dbBooleanTermType) {
        super(name, expectedBaseTypes);
        this.dbBooleanTermType = dbBooleanTermType;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(dbBooleanTermType));
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        /*
         * If unary, tries to lift an IF_ELSE_* above
         */
        Optional<ImmutableFunctionalTerm> firstIfThenTerm = newTerms.stream()
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .filter(t -> t.getFunctionSymbol() instanceof DBIfThenFunctionSymbol)
                .findAny();

        if (firstIfThenTerm.isPresent()) {
            ImmutableFunctionalTerm functionalTerm = firstIfThenTerm.get();
                return ((DBIfThenFunctionSymbol) functionalTerm.getFunctionSymbol()).pushDownExpression(
                        termFactory.getImmutableExpression(this, newTerms), 0, termFactory)
                        .simplify(variableNullability);
        }
        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    @Override
    protected boolean enableIfElseNullLifting() {
        return true;
    }
}
