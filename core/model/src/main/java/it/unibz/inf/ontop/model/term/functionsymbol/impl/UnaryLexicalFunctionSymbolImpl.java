package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;

import java.util.Optional;
import java.util.function.Function;


public class UnaryLexicalFunctionSymbolImpl extends FunctionSymbolImpl {

    private final Function<DBTermType, DBFunctionSymbol> dbFunctionSymbolFct;
    private final DBTermType dbStringType;

    protected UnaryLexicalFunctionSymbolImpl(DBTermType dbStringType, MetaRDFTermType metaRDFTermType,
                                             Function<DBTermType, DBFunctionSymbol> dbFunctionSymbolFct) {
        super("LATELY_TYPE_" + dbFunctionSymbolFct, ImmutableList.of(dbStringType, metaRDFTermType));
        this.dbFunctionSymbolFct = dbFunctionSymbolFct;
        this.dbStringType = dbStringType;
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
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * Could be inferred after simplification
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(dbStringType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm rdfTypeTerm = newTerms.get(1);
        if (rdfTypeTerm instanceof RDFTermTypeConstant) {
            RDFTermType rdfType = ((RDFTermTypeConstant) rdfTypeTerm).getRDFTermType();
            DBTermType dbType = rdfType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory());

            ImmutableFunctionalTerm dbTerm = termFactory.getImmutableFunctionalTerm(
                    dbFunctionSymbolFct.apply(dbType),
                    termFactory.getConversionFromRDFLexical2DB(dbType, newTerms.get(0), rdfType));

            return termFactory.getConversion2RDFLexical(dbType, dbTerm, rdfType)
                    .simplify(variableNullability);
        }

        return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }
}
