package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

public class ExtractLexicalTermFunctionSymbolImpl extends FunctionSymbolImpl implements FunctionSymbol {
    private final DBTermType dbStringType;

    public ExtractLexicalTermFunctionSymbolImpl(RDFTermType abstractRDFTermType, DBTermType dbStringType) {
        super("EXTRACT_LEX", ImmutableList.of(abstractRDFTermType));
        this.dbStringType = dbStringType;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(dbStringType));
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof RDFConstant) {
            return termFactory.getDBStringConstant(((RDFConstant) newTerm).getValue());
        }
        if ((newTerm instanceof ImmutableFunctionalTerm) && ((ImmutableFunctionalTerm) newTerm).getFunctionSymbol() instanceof RDFTermFunctionSymbol)
            return ((ImmutableFunctionalTerm) newTerm).getTerm(0);

        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }
}
