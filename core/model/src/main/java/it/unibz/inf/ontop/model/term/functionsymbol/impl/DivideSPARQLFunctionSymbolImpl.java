package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DivideSPARQLFunctionSymbolImpl extends NumericBinarySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdDecimalType;

    protected DivideSPARQLFunctionSymbolImpl(RDFDatatype abstractNumericType, RDFDatatype xsdDecimalType) {
        super("SP_DIVIDE", SPARQL.NUMERIC_DIVIDE, abstractNumericType);
        this.xsdDecimalType = xsdDecimalType;
    }

    /**
     * Division by zero returns a NULL (SPARQL error) if the operands are both xsd:integer or xsd:decimal
     * (but not if they are xsd:float and xsd:double)
     */
    @Override
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> subLexicalTerms,
                                                                    ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory,
                                                                    VariableNullability variableNullability) {

        Stream<ImmutableExpression> typeTestExpressions = IntStream.range(0, typeTerms.size())
                .mapToObj(i -> termFactory.getIsAExpression(typeTerms.get(i), (RDFTermType) getExpectedBaseType(i)));

        ImmutableExpression denominatorZeroEquality = termFactory.getRDF2DBBooleanFunctionalTerm(
                termFactory.getSPARQLNonStrictEquality(
                        // Denominator
                        termFactory.getRDFFunctionalTerm(subLexicalTerms.get(1), typeTerms.get(1)),
                        termFactory.getRDFLiteralConstant("0.0", xsdDecimalType)));

        // Division by zero case
        ImmutableExpression decimalDivisionByZeroExpression = termFactory.getConjunction(
                termFactory.getIsAExpression(typeTerms.get(0), xsdDecimalType),
                termFactory.getIsAExpression(typeTerms.get(1), xsdDecimalType),
                denominatorZeroEquality);

        return termFactory.getConjunction(Stream.concat(
                                typeTestExpressions,
                                Stream.of(termFactory.getDBNot(decimalDivisionByZeroExpression)))
                        .collect(ImmutableCollectors.toList()))
                .evaluate(variableNullability);
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getCommonPropagatedOrSubstitutedNumericType(
                super.computeTypeTerm(subLexicalTerms, typeTerms, termFactory, variableNullability),
                termFactory.getRDFTermTypeConstant(xsdDecimalType));
    }
}
