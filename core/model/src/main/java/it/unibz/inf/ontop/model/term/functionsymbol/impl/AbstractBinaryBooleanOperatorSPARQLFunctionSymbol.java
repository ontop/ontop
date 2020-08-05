package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractBinaryBooleanOperatorSPARQLFunctionSymbol extends SPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdBooleanType;

    protected AbstractBinaryBooleanOperatorSPARQLFunctionSymbol(@Nonnull String functionSymbolName, @Nonnull String officialName,
                                                                RDFDatatype xsdBooleanType) {
        super(functionSymbolName, officialName, ImmutableList.of(xsdBooleanType, xsdBooleanType));
        this.xsdBooleanType = xsdBooleanType;
    }


    /**
     * Type of the input is not checked as it cannot be a source of SPARQL errors.
     *  {@code  -->} such a type error would correspond to an internal bug.
     *
     *  Effective Boolean Values (EBVs) are expected to be ALREADY wrapped into a specialized function.
     *
     */
    @Override
    protected final ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                           TermFactory termFactory, VariableNullability variableNullability) {
        if (newTerms.stream()
                .allMatch(t -> isRDFFunctionalTerm(t) || (t instanceof Constant))) {

            ImmutableList<ImmutableTerm> subLexicalTerms = newTerms.stream()
                    .map(t -> extractLexicalTerm(t, termFactory))
                    .collect(ImmutableCollectors.toList());

            ImmutableTerm lexicalTerm = computeLexicalTerm(subLexicalTerms, termFactory);

            // The type term only depends on the presence of a non-null lexical term
            ImmutableTerm typeTerm = termFactory.getIfElseNull(
                    termFactory.getDBIsNotNull(lexicalTerm),
                    termFactory.getRDFTermTypeConstant(xsdBooleanType));

            return termFactory.getRDFFunctionalTerm(lexicalTerm, typeTerm);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    private ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, TermFactory termFactory) {
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
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
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

    @Override
    protected boolean tolerateNulls() {
        return true;
    }
}
