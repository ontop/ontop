package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;

import java.util.Optional;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.NOT;

public class RDF2DBBooleanFunctionSymbolImpl extends BooleanFunctionSymbolImpl {

    protected RDF2DBBooleanFunctionSymbolImpl(RDFDatatype xsdBooleanType, DBTermType dbBooleanTermType) {
        super("RDF_2_DB_BOOL", ImmutableList.of(xsdBooleanType), dbBooleanTermType);
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
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return true;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase, TermFactory termFactory) {
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
        // TODO: simplify RDF(..., XSD.BOOLEAN)
        else
            return termFactory.getImmutableExpression(this, newTerms);
    }
}
