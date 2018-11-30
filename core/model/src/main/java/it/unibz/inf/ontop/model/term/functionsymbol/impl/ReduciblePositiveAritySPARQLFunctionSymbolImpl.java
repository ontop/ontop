package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * The SPARQL function must be reducible to DB functions and RDF construction and testing functions
 *
 * Arity >= 1
 */
public abstract class ReduciblePositiveAritySPARQLFunctionSymbolImpl extends FunctionSymbolImpl implements SPARQLFunctionSymbol {

    @Nullable
    private final IRI functionIRI;
    private final String officialName;
    private final BooleanFunctionSymbol isARDFTypeFunctionSymbol;

    protected ReduciblePositiveAritySPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull IRI functionIRI,
                                                             @Nonnull ImmutableList<TermType> expectedBaseTypes,
                                                             BooleanFunctionSymbol isARDFTypeFunctionSymbol) {
        super(functionSymbolName, expectedBaseTypes);
        this.functionIRI = functionIRI;
        this.officialName = functionIRI.getIRIString();
        this.isARDFTypeFunctionSymbol = isARDFTypeFunctionSymbol;
        if (expectedBaseTypes.isEmpty())
            throw new IllegalArgumentException("The arity must be >= 1");
    }

    protected ReduciblePositiveAritySPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull String officialName,
                                                             @Nonnull ImmutableList<TermType> expectedBaseTypes,
                                                             BooleanFunctionSymbol isARDFTypeFunctionSymbol) {
        super(functionSymbolName, expectedBaseTypes);
        this.isARDFTypeFunctionSymbol = isARDFTypeFunctionSymbol;
        this.functionIRI = null;
        this.officialName = officialName;
        if (expectedBaseTypes.isEmpty())
            throw new IllegalArgumentException("The arity must be >= 1");
    }

    @Override
    public Optional<TermTypeInference> inferAndValidateType(ImmutableList<? extends ImmutableTerm> terms)
            throws FatalTypingException {
        validateSubTermTypes(terms);
        return inferType(terms);
    }

    @Override
    protected final ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        if (newTerms.stream()
                .allMatch(t -> isRDFFunctionalTerm(t) || (t instanceof RDFConstant))) {
            ImmutableList<ImmutableTerm> typeTerms = newTerms.stream()
                    .map(t -> extractRDFTermTypeTerm(t, termFactory))
                    .collect(ImmutableCollectors.toList());

            ImmutableExpression.Evaluation inputTypeErrorEvaluation = evaluateInputTypeError(typeTerms, termFactory);
            if (inputTypeErrorEvaluation.getValue().isPresent()) {
                switch (inputTypeErrorEvaluation.getValue().get()) {
                    case FALSE:
                        // SPARQL error --> return NULL
                        return termFactory.getNullConstant();
                    case NULL:
                        throw new MinorOntopInternalBugException("This evaluation (SPARQL type error on the arguments) " +
                                "should not produce a NULL");
                    // TRUE: continue
                    default:
                        break;
                }
            }
            ImmutableTerm lexicalTerm = computeLexicalTerm(newTerms.stream()
                            .map(t -> extractLexicalTerm(t, termFactory))
                            .collect(ImmutableCollectors.toList()),
                        termFactory);

            ImmutableTerm typeTerm = computeTypeTerm(typeTerms, termFactory);

            Optional<ImmutableExpression> condition = inputTypeErrorEvaluation.getExpression();

            return termFactory.getRDFFunctionalTerm(
                    condition
                        .map(c -> (ImmutableTerm) termFactory.getIfElseNull(c, lexicalTerm))
                        .orElse(lexicalTerm),
                    condition
                            .map(c -> (ImmutableTerm) termFactory.getIfElseNull(c, typeTerm))
                            .orElse(typeTerm));
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    private boolean isRDFFunctionalTerm(ImmutableTerm term) {
        return (term instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol);
    }

    private ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> typeTerms,
                                                                  TermFactory termFactory) {
        ImmutableList<ImmutableExpression> typeTestExpressions = typeTerms.stream()
                .map(t -> termFactory.getImmutableExpression(isARDFTypeFunctionSymbol, t))
                .collect(ImmutableCollectors.toList());

         return termFactory.getConjunction(typeTestExpressions)
                 .evaluate(termFactory);
    }

    private ImmutableTerm extractRDFTermTypeTerm(ImmutableTerm rdfTerm, TermFactory termFactory) {
        if (isRDFFunctionalTerm(rdfTerm))
            return ((ImmutableFunctionalTerm)rdfTerm).getTerm(1);
        else if (rdfTerm instanceof RDFConstant)
            return termFactory.getRDFTermTypeConstant(((RDFConstant) rdfTerm).getType());
        throw new IllegalArgumentException("Was expecting a isRDFFunctionalTerm or an RDFConstant");
    }

    private ImmutableTerm extractLexicalTerm(ImmutableTerm rdfTerm, TermFactory termFactory) {
        if (isRDFFunctionalTerm(rdfTerm))
            return ((ImmutableFunctionalTerm)rdfTerm).getTerm(0);
        else if (rdfTerm instanceof RDFConstant)
            return termFactory.getDBStringConstant(((RDFConstant) rdfTerm).getValue());
        throw new IllegalArgumentException("Was expecting a isRDFFunctionalTerm or an RDFConstant");
    }

    /**
     * Compute the lexical term when there is no input type error
     */
    protected abstract ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                                        TermFactory termFactory);

    protected abstract ImmutableTerm computeTypeTerm(ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory);

    @Override
    public Optional<IRI> getIRI() {
        return Optional.ofNullable(functionIRI);
    }

    @Override
    public String getOfficialName() {
        return officialName;
    }
}
