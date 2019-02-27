package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * SPARQL's strict equality function symbol
 */
public class SameTermSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdBooleanType;

    protected SameTermSPARQLFunctionSymbolImpl(@Nonnull RDFTermType rdfRootType, RDFDatatype xsdBooleanType) {

        super("SP_SAME_TERM", SPARQL.SAME_TERM, ImmutableList.of(rdfRootType, rdfRootType));
        this.xsdBooleanType = xsdBooleanType;
    }

    /**
     * Always simplifies itself
     */
    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableExpression condition = termFactory.getStrictEquality(newTerms);

        ImmutableFunctionalTerm lexicalTerm = termFactory.getConversion2RDFLexical(condition, xsdBooleanType);
        ImmutableFunctionalTerm typeTerm = termFactory.getIfElseNull(
                termFactory.getDBIsNotNull(condition),
                termFactory.getRDFTermTypeConstant(xsdBooleanType));

        return termFactory.getRDFFunctionalTerm(lexicalTerm, typeTerm)
                .simplify(variableNullability);
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
}
