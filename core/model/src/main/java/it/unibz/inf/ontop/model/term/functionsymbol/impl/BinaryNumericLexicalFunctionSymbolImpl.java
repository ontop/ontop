package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfThenFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;


public class BinaryNumericLexicalFunctionSymbolImpl extends FunctionSymbolImpl {

    private final String dbNumericOperationName;
    private final DBTermType dbStringType;

    protected BinaryNumericLexicalFunctionSymbolImpl(String dbNumericOperationName, DBTermType dbStringType,
                                                     MetaRDFTermType metaRDFTermType) {
        super("LATELY_TYPE_" + dbNumericOperationName, ImmutableList.of(dbStringType, dbStringType, metaRDFTermType));
        this.dbNumericOperationName = dbNumericOperationName;
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
        ImmutableTerm rdfTypeTerm = newTerms.get(2);
        if (rdfTypeTerm instanceof RDFTermTypeConstant) {
            RDFTermType rdfType = ((RDFTermTypeConstant) rdfTypeTerm).getRDFTermType();
            DBTermType dbType = rdfType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory());

            ImmutableFunctionalTerm numericTerm = termFactory.getDBBinaryNumericFunctionalTerm(
                    dbNumericOperationName, dbType,
                    termFactory.getConversionFromRDFLexical2DB(dbType, newTerms.get(0), rdfType),
                    termFactory.getConversionFromRDFLexical2DB(dbType, newTerms.get(1), rdfType));

            return termFactory.getConversion2RDFLexical(dbType, numericTerm, rdfType);
        }
        else if ((rdfTypeTerm instanceof ImmutableFunctionalTerm)
                && ((ImmutableFunctionalTerm) rdfTypeTerm).getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol) {
            ImmutableFunctionalTerm typeFunctionalTerm = (ImmutableFunctionalTerm) rdfTypeTerm;
            RDFTermTypeFunctionSymbol termTypeFunctionSymbol = (RDFTermTypeFunctionSymbol) typeFunctionalTerm.getFunctionSymbol();

            return termTypeFunctionSymbol.lift(
                    typeFunctionalTerm.getTerms(),
                    c -> buildTermAfterEvaluation(
                            ImmutableList.of(newTerms.get(0), newTerms.get(1), c),
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


}
