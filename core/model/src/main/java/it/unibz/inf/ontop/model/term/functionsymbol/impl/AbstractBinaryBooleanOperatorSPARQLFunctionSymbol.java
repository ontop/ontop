package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractBinaryBooleanOperatorSPARQLFunctionSymbol extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdBooleanType;

    protected AbstractBinaryBooleanOperatorSPARQLFunctionSymbol(@Nonnull String functionSymbolName, @Nonnull String officialName,
                                                                RDFDatatype xsdBooleanType) {
        super(functionSymbolName, officialName, ImmutableList.of(xsdBooleanType));
        this.xsdBooleanType = xsdBooleanType;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        DBTermType dbBooleanType = termFactory.getTypeFactory().getDBTypeFactory().getDBBooleanType();

        ImmutableList<ImmutableExpression> booleanSubTerms = subLexicalTerms.stream()
                .map(t -> termFactory.getConversionFromRDFLexical2DB(dbBooleanType, t, xsdBooleanType))
                .map(t -> (ImmutableExpression) t)
                .collect(ImmutableCollectors.toList());

        return termFactory.getConversion2RDFLexical(
                dbBooleanType,
                computeExpression(booleanSubTerms, termFactory),
                xsdBooleanType);
    }

    protected abstract ImmutableTerm computeExpression(ImmutableList<ImmutableExpression> subExpressions,
                                                       TermFactory termFactory);

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(xsdBooleanType);
    }

    @Override
    protected boolean isAlwaysInjective() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(xsdBooleanType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
