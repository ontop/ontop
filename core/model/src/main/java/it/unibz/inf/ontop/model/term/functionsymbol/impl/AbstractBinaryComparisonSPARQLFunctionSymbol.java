package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;


public abstract class AbstractBinaryComparisonSPARQLFunctionSymbol extends SPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdBooleanType;

    protected AbstractBinaryComparisonSPARQLFunctionSymbol(@Nonnull String functionSymbolName, @Nonnull IRI functionIRI,
                                                           @Nonnull RDFTermType rdfRootType, RDFDatatype xsdBooleanType) {
        super(functionSymbolName, functionIRI, ImmutableList.of(rdfRootType, rdfRootType));
        this.xsdBooleanType = xsdBooleanType;
    }

    protected AbstractBinaryComparisonSPARQLFunctionSymbol(@Nonnull String functionSymbolName, @Nonnull String officialName,
                                                           @Nonnull RDFTermType rdfRootType, RDFDatatype xsdBooleanType) {
        super(functionSymbolName, officialName, ImmutableList.of(rdfRootType, rdfRootType));
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
        if (newTerms.stream()
                .allMatch(t -> isRDFFunctionalTerm(t) || (t instanceof Constant))) {
            ImmutableList<ImmutableTerm> typeTerms = newTerms.stream()
                    .map(t -> extractRDFTermTypeTerm(t, termFactory))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ImmutableTerm> subLexicalTerms = newTerms.stream()
                    .map(t -> extractLexicalTerm(t, termFactory))
                    .collect(ImmutableCollectors.toList());

            ImmutableTerm lexicalTerm = computeLexicalTerm(subLexicalTerms, typeTerms, termFactory)
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

    protected abstract ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                                        ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory);
}
