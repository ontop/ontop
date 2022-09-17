package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * The SPARQL function must be reducible to DB functions and RDF construction and testing functions
 *
 * Arity {@code >= 1 }
 */
public abstract class ReduciblePositiveAritySPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl {

    protected ReduciblePositiveAritySPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull IRI functionIRI,
                                                             @Nonnull ImmutableList<TermType> expectedBaseTypes) {
        super(functionSymbolName, functionIRI, expectedBaseTypes);
        if (expectedBaseTypes.isEmpty())
            throw new IllegalArgumentException("The arity must be >= 1");
    }

    protected ReduciblePositiveAritySPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull String officialName,
                                                             @Nonnull ImmutableList<TermType> expectedBaseTypes) {
        super(functionSymbolName, officialName, expectedBaseTypes);
        if (expectedBaseTypes.isEmpty())
            throw new IllegalArgumentException("The arity must be >= 1");
    }

    @Override
    protected final ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                           TermFactory termFactory, VariableNullability variableNullability) {
        if ((!tolerateNulls()
                && newTerms.stream().anyMatch(t -> (t instanceof Constant) && t.isNull())))
            return termFactory.getNullConstant();

        if (newTerms.stream()
                .allMatch(t -> isRDFFunctionalTerm(t) || (t instanceof Constant))) {
            ImmutableList<ImmutableTerm> typeTerms = newTerms.stream()
                    .map(t -> extractRDFTermTypeTerm(t, termFactory))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ImmutableTerm> subLexicalTerms = newTerms.stream()
                    .map(t -> extractLexicalTerm(t, termFactory))
                    .collect(ImmutableCollectors.toList());

            ImmutableExpression.Evaluation inputTypeErrorEvaluation = evaluateInputTypeError(subLexicalTerms, typeTerms,
                    termFactory, variableNullability);

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

            ImmutableTerm typeTerm = computeTypeTerm(subLexicalTerms, typeTerms, termFactory, variableNullability);
            ImmutableTerm lexicalTerm = computeLexicalTerm(subLexicalTerms, typeTerms, termFactory, typeTerm);

            Optional<ImmutableExpression> inputErrorCondition = inputTypeErrorEvaluation.getExpression();

            ImmutableExpression nonNullLexicalTermCondition = termFactory.getDBIsNotNull(lexicalTerm);

            ImmutableExpression typeCondition = inputErrorCondition
                    .map(c -> termFactory.getConjunction(c, nonNullLexicalTermCondition))
                    .orElse(nonNullLexicalTermCondition);

            return termFactory.getRDFFunctionalTerm(
                    inputErrorCondition
                        .map(c -> (ImmutableTerm) termFactory.getIfElseNull(c, lexicalTerm))
                        .orElse(lexicalTerm),
                    termFactory.getIfElseNull(typeCondition, typeTerm));
        }
        else
            return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    /**
     * By default, does not tolerate receiving NULLs (SPARQL errors) as input
     */
    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    /**
     * MUST detect ALL the cases where the SPARQL function would produce an error (that is a NULL)
     * {@code ---> } the resulting condition must determine if the output of the SPARQL function is NULL (evaluates to FALSE or NULL)
     *      or not (evaluates to TRUE).
     *
     * Default implementation, can be overridden
     *
     */
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableList<ImmutableExpression> typeTestExpressions = IntStream.range(0, typeTerms.size())
                .mapToObj(i -> termFactory.getIsAExpression(typeTerms.get(i), (RDFTermType) getExpectedBaseType(i)))
                .collect(ImmutableCollectors.toList());

         return termFactory.getConjunction(typeTestExpressions)
                 .evaluate(variableNullability);
    }

    /**
     * Compute the lexical term when there is no input type error
     */
    protected abstract ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                                        ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                                        ImmutableTerm returnedTypeTerm);

    protected abstract ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                                     ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability);

}
