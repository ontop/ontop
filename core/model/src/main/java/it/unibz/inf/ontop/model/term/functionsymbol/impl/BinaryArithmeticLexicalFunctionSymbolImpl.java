package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;

import java.util.Optional;

public class BinaryArithmeticLexicalFunctionSymbolImpl extends FunctionSymbolImpl{

    private final String dbOperationName;
    private final DBTermType dbStringType;
    private final TypeFactory typeFactory;

    protected BinaryArithmeticLexicalFunctionSymbolImpl(String dbOperationName, DBTermType dbStringType,
                                                        MetaRDFTermType metaRDFTermType, TypeFactory typeFactory) {
        super("LEXICAL_BINARY_" + dbOperationName, ImmutableList.of(dbStringType, dbStringType, metaRDFTermType, metaRDFTermType, metaRDFTermType));
        this.dbOperationName = dbOperationName;
        this.dbStringType = dbStringType;
        this.typeFactory = typeFactory;
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
        ImmutableTerm rdfTypeTerm = newTerms.get(4);
        if (rdfTypeTerm instanceof RDFTermTypeConstant) {
            RDFTermType rdfType = ((RDFTermTypeConstant) rdfTypeTerm).getRDFTermType();

            if (rdfType.isA(termFactory.getTypeFactory().getAbstractOntopNumericDatatype())) {
                return getNumericLexicalTerm(newTerms, termFactory, rdfType);
            } else {
                return termFactory.getNullConstant();
            }
        }
        else if ((rdfTypeTerm instanceof ImmutableFunctionalTerm)
                && ((ImmutableFunctionalTerm) rdfTypeTerm).getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol) {
            ImmutableFunctionalTerm typeFunctionalTerm = (ImmutableFunctionalTerm) rdfTypeTerm;
            RDFTermTypeFunctionSymbol termTypeFunctionSymbol = (RDFTermTypeFunctionSymbol) typeFunctionalTerm.getFunctionSymbol();

            return termTypeFunctionSymbol.lift(
                            typeFunctionalTerm.getTerms(),
                            c -> buildTermAfterEvaluation(
                                    ImmutableList.of(newTerms.get(0), newTerms.get(1), newTerms.get(2), newTerms.get(3), c),
                                    termFactory, variableNullability),
                            termFactory)
                    .simplify(variableNullability);
        }
        else
            // Tries to lift the DB case of the rdf type term if there is any
            return Optional.of(rdfTypeTerm)
                    .filter(t -> t instanceof ImmutableFunctionalTerm)
                    .map(t -> (ImmutableFunctionalTerm)t)
                    .filter(t -> t.getFunctionSymbol() instanceof DBIfThenFunctionSymbol)
                    .map(t -> ((DBIfThenFunctionSymbol) t.getFunctionSymbol())
                            .pushDownRegularFunctionalTerm(
                                    termFactory.getImmutableFunctionalTerm(this, newTerms),
                                    newTerms.indexOf(t),
                                    termFactory))
                    .map(t -> t.simplify(variableNullability))
                    .orElseGet(() -> super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability));
    }

    private ImmutableTerm getNumericLexicalTerm(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                RDFTermType rdfType) {
        DBTermType dbType = rdfType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory());

        ImmutableFunctionalTerm numericTerm = termFactory.getDBBinaryNumericFunctionalTerm(
                dbOperationName, dbType,
                termFactory.getConversionFromRDFLexical2DB(dbType, newTerms.get(0), rdfType),
                termFactory.getConversionFromRDFLexical2DB(dbType, newTerms.get(1), rdfType));

        return termFactory.getConversion2RDFLexical(dbType, numericTerm, rdfType);
    }
}
