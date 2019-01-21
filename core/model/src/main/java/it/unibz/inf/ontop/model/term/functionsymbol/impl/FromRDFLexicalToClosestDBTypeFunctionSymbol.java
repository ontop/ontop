package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

public class FromRDFLexicalToClosestDBTypeFunctionSymbol extends FunctionSymbolImpl implements FunctionSymbol {

    protected FromRDFLexicalToClosestDBTypeFunctionSymbol(DBTermType dbStringType, MetaRDFTermType metaRDFTermType) {
        super("TO_CLOSEST_DB_TYPE", ImmutableList.of(dbStringType, metaRDFTermType));
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    protected boolean isAlwaysInjective() {
        return false;
    }

    /**
     * TODO: could try to infer it when the second argument is a constant
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    /**
     * TODO: simplify in the presence of magic numbers
     */
    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm rdfTypeTerm = newTerms.get(1);
        if (rdfTypeTerm instanceof RDFTermTypeConstant) {
            RDFTermType rdfType = ((RDFTermTypeConstant) rdfTypeTerm).getRDFTermType();
            DBTermType dbType = rdfType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory());

            return termFactory.getConversionFromRDFLexical2DB(dbType, newTerms.get(0), rdfType);
        }

        return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }
}
