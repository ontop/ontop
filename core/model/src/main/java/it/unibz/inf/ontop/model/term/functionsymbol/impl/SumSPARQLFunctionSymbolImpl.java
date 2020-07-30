package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XSD;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SumSPARQLFunctionSymbolImpl extends SumLikeSPARQLAggregationFunctionSymbolImpl implements SPARQLAggregationFunctionSymbol {

    protected SumSPARQLFunctionSymbolImpl(boolean isDistinct, RDFTermType rootRdfTermType) {
        super("SP_SUM"+ (isDistinct ? "_DISTINCT" : ""), SPARQL.SUM, isDistinct, rootRdfTermType, "sum1");
    }

    @Override
    protected ImmutableFunctionalTerm createAggregate(ConcreteNumericRDFDatatype rdfType, ImmutableTerm dbTerm,
                                                      TermFactory termFactory) {

        DBTermType dbType = rdfType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory());
        return termFactory.getDBSum(dbTerm, dbType, isDistinct());
    }

    @Override
    protected ImmutableTerm combineAggregates(ImmutableTerm aggregate1, ImmutableTerm aggregate2, TermFactory termFactory) {
        DBTermType dbDecimalType = termFactory.getTypeFactory().getDBTypeFactory().getDBDecimalType();
        return termFactory.getDBBinaryNumericFunctionalTerm(SPARQL.NUMERIC_ADD, dbDecimalType, aggregate1, aggregate2);
    }

    @Override
    protected ConcreteNumericRDFDatatype inferTypeWhenNonEmpty(ConcreteNumericRDFDatatype inputNumericDatatype, TypeFactory typeFactory) {
        return inputNumericDatatype.getCommonPropagatedOrSubstitutedType(inputNumericDatatype);
    }

    @Override
    protected ImmutableTerm getNeutralElement(TermFactory termFactory) {
        return termFactory.getDBIntegerConstant(0);
    }

    @Override
    public Constant evaluateEmptyBag(TermFactory termFactory) {
        return termFactory.getRDFLiteralConstant("0", XSD.INTEGER);
    }
}
