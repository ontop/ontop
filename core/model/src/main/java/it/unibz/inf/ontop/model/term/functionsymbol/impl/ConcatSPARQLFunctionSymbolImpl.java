package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Arity >= 2
 */
public class ConcatSPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    protected ConcatSPARQLFunctionSymbolImpl(int arity, RDFDatatype xsdStringType, BooleanFunctionSymbol isARDFTypeFunctionSymbol) {
        super("SP_CONCAT" + arity, XPathFunction.CONCAT,
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> (TermType) xsdStringType)
                        .collect(ImmutableCollectors.toList()), isARDFTypeFunctionSymbol);
        if (arity < 2)
            throw new IllegalArgumentException("CONCAT arity must be >= 2");
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, TermFactory termFactory) {
        return termFactory.getConcatFunctionalTerm(subLexicalTerms);
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getCommonDenominatorFunctionalTerm(typeTerms)
                // NB: isInConstructionNodeInOptimizationPhase is irrelevant here
                .simplify(false);
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return arguments.stream()
                .filter(t -> !(t instanceof Constant))
                .count() <= 1;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        throw new RuntimeException("TODO: implement ConcatSPARQLFunctionSymbolImpl.inferType(...)");
    }

    /**
     * Could be allowed in the future
     */
    @Override
    public boolean canBePostProcessed() {
        return false;
    }
}
