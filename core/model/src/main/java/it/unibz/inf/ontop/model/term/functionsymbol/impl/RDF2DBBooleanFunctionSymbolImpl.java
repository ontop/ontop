package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;

import java.util.Optional;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.NOT;

public class RDF2DBBooleanFunctionSymbolImpl extends BooleanFunctionSymbolImpl {

    private final DBTermType dbBooleanTermType;
    private final DBTermType dbStringTermType;

    protected RDF2DBBooleanFunctionSymbolImpl(RDFDatatype xsdBooleanType, DBTermType dbBooleanTermType,
                                              DBTermType dbStringTermType) {
        super("RDF_2_DB_BOOL", ImmutableList.of(xsdBooleanType), dbBooleanTermType);
        this.dbBooleanTermType = dbBooleanTermType;
        this.dbStringTermType = dbStringTermType;
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        return termFactory.getImmutableExpression(NOT, termFactory.getImmutableExpression(this, subTerms));
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
                                                     boolean isInConstructionNodeInOptimizationPhase, TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof Constant) {
            Constant newConstant = (Constant) newTerm;
            return Optional.of(newConstant)
                    .filter(c -> c instanceof NonNullConstant)
                    .map(c -> (NonNullConstant) c)
                    .map(c -> c.getValue().toLowerCase().equals("true"))
                    .map(b -> (ImmutableTerm) termFactory.getDBBooleanConstant(b))
                    .orElseGet(termFactory::getNullConstant);
        }
        else if ((newTerm instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) newTerm).getFunctionSymbol()) instanceof RDFTermFunctionSymbol) {
            // TODO: shall we check the RDF datatype?
            ImmutableTerm lexicalTerm = ((ImmutableFunctionalTerm) newTerm).getTerm(0);
            return termFactory.getDBCastFunctionalTerm(dbStringTermType, dbBooleanTermType, lexicalTerm)
                    .simplify(isInConstructionNodeInOptimizationPhase, variableNullability);
        }
        else
            return termFactory.getImmutableExpression(this, newTerms);
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }
}
