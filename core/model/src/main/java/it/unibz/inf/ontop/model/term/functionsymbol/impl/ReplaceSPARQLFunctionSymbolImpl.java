package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;

public class ReplaceSPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdStringType;

    protected ReplaceSPARQLFunctionSymbolImpl(int arity, RDFDatatype xsdStringType) {
        super("SP_REPLACE_" + arity, XPathFunction.REPLACE,
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> xsdStringType)
                        .collect(ImmutableCollectors.toList()));
        this.xsdStringType = xsdStringType;

        if (arity < 3 || arity > 4)
            throw new IllegalArgumentException("The arity of REPLACE must be 3 or 4");
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return getArity() == 3
                ? termFactory.getDBReplace(subLexicalTerms.get(0), subLexicalTerms.get(1), subLexicalTerms.get(2))
                : termFactory.getDBRegexpReplace(subLexicalTerms.get(0), subLexicalTerms.get(1),
                    subLexicalTerms.get(2), subLexicalTerms.get(3));
    }

    /**
     * Not clear in the SPARQL specification. Return the type of the first argument or XSD.STRING?
     * TODO: clarify
     */
    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return typeTerms.get(0);
    }

    @Override
    protected boolean isAlwaysInjective() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return terms.get(0).inferType()
                .filter(i -> i.getTermType()
                        .map(t -> t.isA(xsdStringType))
                        .orElse(false));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    /**
     * Enforces that the arguments, except the first one, are SIMPLE XSD.STRING (not langStrings)
     */
    @Override
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory, VariableNullability variableNullability) {

        RDFTermTypeConstant xsdStringConstant = termFactory.getRDFTermTypeConstant(xsdStringType);

        return termFactory.getConjunction(
                typeTerms.stream()
                        .skip(1)
                        .map(t -> termFactory.getStrictEquality(t, xsdStringConstant)))
                .get()
                .evaluate(termFactory, variableNullability);
    }
}
