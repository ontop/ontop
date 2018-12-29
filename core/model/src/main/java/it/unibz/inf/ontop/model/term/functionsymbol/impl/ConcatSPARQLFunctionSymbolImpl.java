package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Arity >= 2
 */
public class ConcatSPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdStringType;

    protected ConcatSPARQLFunctionSymbolImpl(int arity, RDFDatatype xsdStringType) {
        super("SP_CONCAT" + arity, XPathFunction.CONCAT,
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> (TermType) xsdStringType)
                        .collect(ImmutableCollectors.toList()));
        this.xsdStringType = xsdStringType;
        if (arity < 2)
            throw new IllegalArgumentException("CONCAT arity must be >= 2");
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, TermFactory termFactory) {
        return termFactory.getDBConcatFunctionalTerm(subLexicalTerms);
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        ImmutableExpression condition = termFactory.getStrictEquality(typeTerms);

        return termFactory.getDBCase(
                Stream.of(Maps.immutableEntry(condition, typeTerms.get(0))),
                termFactory.getRDFTermTypeConstant(xsdStringType))
                // NB: isInConstructionNodeInOptimizationPhase is irrelevant here
                .simplify(false, variableNullability);
    }

    @Override
    protected boolean isAlwaysInjective() {
        return false;
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
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
