package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class DefaultDBNotFunctionSymbol extends DBBooleanFunctionSymbolImpl {

    private final String prefix;

    protected DefaultDBNotFunctionSymbol(@Nonnull String nameInDialect, DBTermType dbBooleanTermType) {
        super(nameInDialect, ImmutableList.of(dbBooleanTermType), dbBooleanTermType);
        prefix = String.format("%s ", nameInDialect);
    }

    @Override
    public boolean blocksNegation() {
        return false;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        return (ImmutableExpression) subTerms.get(0);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return inBrackets(prefix + termConverter.apply(terms.get(0)));
    }

    @Override
    protected boolean isAlwaysInjective() {
        return true;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory, VariableNullability variableNullability) {

        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof Constant)
            return newTerm.isNull()
                    ? newTerm
                    : termFactory.getDBBooleanConstant(newTerm.equals(termFactory.getDBBooleanConstant(false)));

        return super.buildTermAfterEvaluation(newTerms, isInConstructionNodeInOptimizationPhase, termFactory, variableNullability);
    }
}
