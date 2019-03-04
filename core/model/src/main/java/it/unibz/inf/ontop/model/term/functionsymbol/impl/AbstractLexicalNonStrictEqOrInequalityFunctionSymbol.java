package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;

import java.util.Optional;

public abstract class AbstractLexicalNonStrictEqOrInequalityFunctionSymbol extends BooleanFunctionSymbolImpl {

    private final RDFDatatype xsdBooleanType;
    private final RDFDatatype xsdDateTimeType;
    private final RDFDatatype xsdStringType;

    protected AbstractLexicalNonStrictEqOrInequalityFunctionSymbol(String functionSymbolName, MetaRDFTermType metaRDFTermType,
                                                                   RDFDatatype xsdBooleanType, RDFDatatype xsdDateTimeType,
                                                                   RDFDatatype xsdStringType,
                                                                   DBTermType dbStringType, DBTermType dbBooleanType) {
        super(functionSymbolName,
                ImmutableList.of(dbStringType, metaRDFTermType, dbStringType, metaRDFTermType),
                dbBooleanType);
        this.xsdBooleanType = xsdBooleanType;
        this.xsdDateTimeType = xsdDateTimeType;
        this.xsdStringType = xsdStringType;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        ImmutableTerm lexicalTerm1 = newTerms.get(0);
        ImmutableTerm lexicalTerm2 = newTerms.get(2);
        ImmutableTerm typeTerm1 = unwrapIfElseNull(newTerms.get(1));
        ImmutableTerm typeTerm2 = unwrapIfElseNull(newTerms.get(3));
        /*
         * Simplifies when both type terms are constant
         */
        if ((typeTerm1 instanceof RDFTermTypeConstant) && (typeTerm2 instanceof RDFTermTypeConstant)){
            RDFTermType termType1 = ((RDFTermTypeConstant)typeTerm1).getRDFTermType();
            RDFTermType termType2 = ((RDFTermTypeConstant)typeTerm2).getRDFTermType();

            ImmutableTerm dbTerm1 = termFactory.getConversionFromRDFLexical2DB(
                    termType1.getClosestDBType(dbTypeFactory), lexicalTerm1, termType1);

            ImmutableTerm dbTerm2 = termFactory.getConversionFromRDFLexical2DB(
                    termType1.getClosestDBType(dbTypeFactory), lexicalTerm2, termType2);

            if ((termType1 instanceof ConcreteNumericRDFDatatype) && (termType2 instanceof ConcreteNumericRDFDatatype))
                return computeNumericEqualityOrInequality(dbTerm1, dbTerm2, termFactory, variableNullability);

            else if (termType1.equals(termType2)) {
                if (termType1.equals(xsdBooleanType))
                    return computeBooleanEqualityOrInequality(dbTerm1, dbTerm2, termFactory, variableNullability);
                else if (termType1.equals(xsdStringType))
                    return computeStringEqualityOrInequality(dbTerm1, dbTerm2, termFactory, variableNullability);
                else if (termType1.equals(xsdDateTimeType))
                    return computeDatetimeEqualityOrInequality(dbTerm1, dbTerm2, termFactory, variableNullability);
                else
                    return computeDefaultSameTypeEqualityOrInequality(termType1, dbTerm1, dbTerm2, termFactory,
                            variableNullability);
            }
            else
                return computeDefaultDifferentTypeEqualityOrInequality(termType1, termType2, termFactory);
        }
        else
            return termFactory.getImmutableExpression(this, lexicalTerm1, typeTerm1, lexicalTerm2, typeTerm2);
    }

    private ImmutableTerm unwrapIfElseNull(ImmutableTerm term) {
        return Optional.of(term)
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .filter(t -> t.getFunctionSymbol() instanceof DBIfElseNullFunctionSymbol)
                .map(t -> t.getTerm(1))
                .orElse(term);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }

    protected abstract ImmutableTerm computeNumericEqualityOrInequality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2,
                                                                        TermFactory termFactory, VariableNullability variableNullability);

    protected abstract ImmutableTerm computeBooleanEqualityOrInequality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2,
                                                                        TermFactory termFactory, VariableNullability variableNullability);

    protected abstract ImmutableTerm computeStringEqualityOrInequality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2, TermFactory termFactory,
                                                                       VariableNullability variableNullability);

    protected abstract ImmutableTerm computeDatetimeEqualityOrInequality(ImmutableTerm dbTerm1, ImmutableTerm dbTerm2,
                                                                         TermFactory termFactory,
                                                                         VariableNullability variableNullability);

    protected abstract ImmutableTerm computeDefaultSameTypeEqualityOrInequality(RDFTermType termType, ImmutableTerm dbTerm1,
                                                                                ImmutableTerm dbTerm2, TermFactory termFactory,
                                                                                VariableNullability variableNullability);

    protected abstract ImmutableTerm computeDefaultDifferentTypeEqualityOrInequality(RDFTermType termType1, RDFTermType termType2,
                                                                                     TermFactory termFactory);
}
