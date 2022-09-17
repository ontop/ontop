package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;

public class CoalesceSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl {

    protected CoalesceSPARQLFunctionSymbolImpl(int arity, RDFTermType rootRDFTermType) {
        super("SP_COALESCE" + arity, SPARQL.COALESCE,
                IntStream.range(0, arity)
                        .mapToObj(i -> rootRDFTermType)
                        .collect(ImmutableCollectors.toList()));
    }

    @Override
    protected final ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                           TermFactory termFactory, VariableNullability variableNullability) {
        if (newTerms.stream().allMatch(ImmutableTerm::isNull))
            return termFactory.getNullConstant();

        if (newTerms.stream()
                .allMatch(t -> isRDFFunctionalTerm(t) || (t instanceof Constant))) {
            ImmutableList<ImmutableTerm> typeTerms = newTerms.stream()
                    .map(t -> extractRDFTermTypeTerm(t, termFactory))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ImmutableTerm> subLexicalTerms = newTerms.stream()
                    .map(t -> extractLexicalTerm(t, termFactory))
                    .collect(ImmutableCollectors.toList());

            ImmutableTerm typeTerm = termFactory.getDBCoalesce(typeTerms);
            ImmutableTerm lexicalTerm = termFactory.getDBCoalesce(subLexicalTerms);

            return termFactory.getRDFFunctionalTerm(lexicalTerm, typeTerm)
                    .simplify(variableNullability);
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * No inference at the moment, because it is too complex
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
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
