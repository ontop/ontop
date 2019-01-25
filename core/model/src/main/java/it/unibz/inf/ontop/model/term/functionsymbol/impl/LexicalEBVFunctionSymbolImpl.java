package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.*;

/**
 * https://www.w3.org/TR/sparql11-query/#ebv
 *
 * Limitation:
 *   - We don't look for invalid lexical form for booleans and numerics
 *
 */
public class LexicalEBVFunctionSymbolImpl extends BooleanFunctionSymbolImpl {


    protected LexicalEBVFunctionSymbolImpl(DBTermType dbStringType, MetaRDFTermType metaRDFTermType,
                                           DBTermType dbBooleanTermType) {
        super("LEX_EBV", ImmutableList.of(dbStringType, metaRDFTermType), dbBooleanTermType);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
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
    protected boolean isAlwaysInjective() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        if ((newTerms.get(0) instanceof DBConstant) && (newTerms.get(1) instanceof RDFTermTypeConstant)) {
            DBConstant lexicalConstant = (DBConstant) newTerms.get(0);
            RDFTermType rdfTermType = ((RDFTermTypeConstant) newTerms.get(1)).getRDFTermType();

            return computeEBV(lexicalConstant, rdfTermType, termFactory)
                    .simplify(variableNullability);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    private ImmutableTerm computeEBV(DBConstant lexicalConstant, RDFTermType rdfTermType, TermFactory termFactory) {
        TypeFactory typeFactory = termFactory.getTypeFactory();
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        RDFDatatype abstractNumeric = typeFactory.getAbstractOntopNumericDatatype();
        RDFDatatype xsdFloat = typeFactory.getXsdFloatDatatype();
        RDFDatatype xsdDouble = typeFactory.getXsdDoubleDatatype();
        RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();

        if (rdfTermType.isA(xsdString)) {
            return computeStringEBV(lexicalConstant, termFactory);
        }
        else if (rdfTermType.isA(xsdFloat) || rdfTermType.isA(xsdDouble))
            return computeFloatOrDoubleEBV(lexicalConstant, rdfTermType, termFactory);
        else if (rdfTermType.isA(abstractNumeric)) {
            return computeOtherNumericEBV(lexicalConstant, rdfTermType, termFactory);
        }
        else if (rdfTermType.isA(xsdBoolean))
            return computeBooleanEBV(lexicalConstant, rdfTermType, termFactory);
        else
            return termFactory.getNullConstant();
    }

    private ImmutableTerm computeStringEBV(DBConstant lexicalConstant, TermFactory termFactory) {
        return termFactory.getDBNot(termFactory.getDBIsStringEmpty(lexicalConstant));
    }

    private ImmutableExpression computeFloatOrDoubleEBV(DBConstant lexicalConstant, RDFTermType rdfTermType,
                                                  TermFactory termFactory) {
        ImmutableFunctionalTerm dbNumericTerm = termFactory.getConversionFromRDFLexical2DB(lexicalConstant, rdfTermType);

        ImmutableExpression notANumberCondition = termFactory.getDBNonStrictNumericEquality(dbNumericTerm,
                termFactory.getDoubleNaN());

        return termFactory.getConjunction(
                computeOtherNumericEBV(lexicalConstant, rdfTermType, termFactory),
                termFactory.getDBNot(notANumberCondition));
    }

    private ImmutableExpression computeOtherNumericEBV(DBConstant lexicalConstant, RDFTermType rdfTermType,
                                                 TermFactory termFactory) {
        ImmutableFunctionalTerm dbNumericTerm = termFactory.getConversionFromRDFLexical2DB(lexicalConstant, rdfTermType);

        return termFactory.getDBNot(
                termFactory.getDBNonStrictNumericEquality(dbNumericTerm, termFactory.getDBIntegerConstant(0)));
    }

    private ImmutableTerm computeBooleanEBV(DBConstant lexicalConstant, RDFTermType rdfTermType, TermFactory termFactory) {
        return termFactory.getConversionFromRDFLexical2DB(lexicalConstant, rdfTermType);
    }
}
