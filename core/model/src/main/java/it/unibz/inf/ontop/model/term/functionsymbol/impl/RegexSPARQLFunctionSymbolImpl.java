package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RegexSPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdStringType;
    private final RDFDatatype xsdBooleanType;

    protected RegexSPARQLFunctionSymbolImpl(int arity, RDFDatatype xsdStringType, RDFDatatype xsdBooleanType) {
        super("SP_REGEX_" + arity, SPARQL.REGEX,
                IntStream.range(0, arity)
                        .mapToObj(i -> xsdStringType)
                        .collect(ImmutableCollectors.toList()));
        this.xsdStringType = xsdStringType;
        this.xsdBooleanType = xsdBooleanType;

        if (arity < 2 || arity > 3)
            throw new IllegalArgumentException("The arity of REGEX must be 2 or 3");
    }

    /**
     * TODO: shall we consider using a DB-specific converting function for the flags, or is POSIX enough?
     */
    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getConversion2RDFLexical(
                dbTypeFactory.getDBBooleanType(),
                termFactory.getDBRegexpMatches(subLexicalTerms),
                xsdBooleanType);
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(xsdBooleanType);
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

    /**
     * Enforces that the arguments, except the first one, are SIMPLE XSD.STRING (not langStrings)
     */
    @Override
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory, VariableNullability variableNullability) {

        RDFTermTypeConstant xsdStringConstant = termFactory.getRDFTermTypeConstant(xsdStringType);

        ImmutableList<ImmutableExpression> conditions = Stream.concat(
                Stream.of(termFactory.getIsAExpression(typeTerms.get(0), xsdStringType)),
                typeTerms.stream()
                        .skip(1)
                        .map(t -> termFactory.getStrictEquality(t, xsdStringConstant)))
                .collect(ImmutableCollectors.toList());

        return termFactory.getConjunction(conditions)
                .evaluate(variableNullability);
    }
}
