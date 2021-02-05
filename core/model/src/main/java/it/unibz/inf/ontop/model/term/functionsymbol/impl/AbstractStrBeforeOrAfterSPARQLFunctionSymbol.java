package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractStrBeforeOrAfterSPARQLFunctionSymbol extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdStringType;

    protected AbstractStrBeforeOrAfterSPARQLFunctionSymbol(@Nonnull String functionSymbolName, @Nonnull IRI functionIRI, RDFDatatype xsdStringType) {
        super(functionSymbolName, functionIRI, ImmutableList.of(xsdStringType, xsdStringType));
        this.xsdStringType = xsdStringType;
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        ImmutableTerm firstArgType = typeTerms.get(0);
        if ((firstArgType instanceof RDFTermTypeConstant)
                && ((RDFTermTypeConstant) firstArgType).getRDFTermType().equals(xsdStringType))
            return firstArgType;

        ImmutableTerm firstArg = subLexicalTerms.get(0);
        ImmutableTerm secondArg = subLexicalTerms.get(1);

        ImmutableExpression condition =
                termFactory.getDisjunction(ImmutableList.of(
                        termFactory.getDBContains(ImmutableList.of(firstArg, secondArg)),
                        termFactory.getDBIsStringEmpty(secondArg)));

        return termFactory.getIfThenElse(condition, firstArgType, termFactory.getRDFTermTypeConstant(xsdStringType));
    }

    protected abstract ImmutableTerm computeLexicalTermWhenSecondArgIsNotEmpty(ImmutableList<ImmutableTerm> subLexicalTerms,
                                                                               TermFactory termFactory);

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * Too complicated logic to determine if it is an XSD.STRING or a langString
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
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getAreCompatibleRDFStringExpression(typeTerms.get(0), typeTerms.get(1))
                .evaluate(variableNullability);
    }
}
