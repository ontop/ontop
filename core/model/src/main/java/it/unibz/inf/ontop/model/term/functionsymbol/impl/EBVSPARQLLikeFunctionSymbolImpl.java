package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import javax.annotation.Nonnull;
import java.util.Optional;


public class EBVSPARQLLikeFunctionSymbolImpl extends SPARQLLikeFunctionSymbolImpl {

    private final RDFDatatype xsdBooleanType;

    protected EBVSPARQLLikeFunctionSymbolImpl(@Nonnull RDFDatatype rootDatatype, RDFDatatype xsdBooleanType) {
        super("SP_EBV", ImmutableList.of(rootDatatype));
        this.xsdBooleanType = xsdBooleanType;
    }
    @Override
    protected boolean tolerateNulls() {
        return false;
    }

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
    protected final ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                           TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);

        if (isRDFFunctionalTerm(newTerm) || (newTerm instanceof Constant)) {

            ImmutableTerm subLexicalTerm = extractLexicalTerm(newTerm, termFactory);
            ImmutableTerm subTypeTerm = extractRDFTermTypeTerm(newTerm, termFactory);

            ImmutableTerm lexicalTerm = computeLexicalTerm(subLexicalTerm, subTypeTerm, termFactory)
                    .simplify(variableNullability);

            // NB: Only there for improving the performance
            if (lexicalTerm.equals(termFactory.getNullConstant()))
                return lexicalTerm;

            ImmutableTerm typeTerm = termFactory.getIfElseNull(
                    termFactory.getDBIsNotNull(lexicalTerm),
                    termFactory.getRDFTermTypeConstant(xsdBooleanType))
                    .simplify(variableNullability);

            return termFactory.getRDFFunctionalTerm(lexicalTerm, typeTerm);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    protected ImmutableTerm computeLexicalTerm(ImmutableTerm subLexicalTerm, ImmutableTerm subTypeTerm, TermFactory termFactory) {
        return termFactory.getConversion2RDFLexical(
                termFactory.getLexicalEffectiveBooleanValue(subLexicalTerm, subTypeTerm),
                xsdBooleanType);
    }
}
