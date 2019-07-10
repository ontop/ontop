package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.ConcreteNumericRDFDatatype;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.VariableGenerator;


public class AvgSPARQLFunctionSymbolImpl extends UnaryNumericSPARQLAggregationFunctionSymbolImpl {

    protected AvgSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isDistinct) {
        super("SP_AVG", SPARQL.AVG, isDistinct, rootRdfTermType, "avg1");
    }

    @Override
    protected ConcreteNumericRDFDatatype inferTypeWhenNonEmpty(ConcreteNumericRDFDatatype inputNumericDatatype, TypeFactory typeFactory) {
        ConcreteNumericRDFDatatype propagatedOrSubstitutedType = inputNumericDatatype.getCommonPropagatedOrSubstitutedType(
                inputNumericDatatype);

        return propagatedOrSubstitutedType.equals(typeFactory.getXsdIntegerDatatype())
                ? typeFactory.getXsdDecimalDatatype()
                : propagatedOrSubstitutedType;
    }

    @Override
    protected AggregationSimplification decomposeMultityped(ImmutableTerm subTerm,
                                                            ImmutableSet<RDFTermType> subTermPossibleTypes,
                                                            boolean hasGroupBy, VariableNullability variableNullability,
                                                            VariableGenerator variableGenerator, TermFactory termFactory) {
        throw new RuntimeException("TODO: implement decomposeMultityped(...)");
    }

    @Override
    protected ImmutableFunctionalTerm createAggregate(ConcreteNumericRDFDatatype rdfType, ImmutableTerm dbTerm,
                                                      TermFactory termFactory) {
        DBTermType dbType = rdfType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory());
        return termFactory.getDBAvg(dbTerm, dbType, isDistinct());
    }
}
