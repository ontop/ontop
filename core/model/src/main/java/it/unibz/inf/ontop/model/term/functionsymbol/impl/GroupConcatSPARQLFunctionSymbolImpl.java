package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.IntStream;

public class GroupConcatSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl implements SPARQLAggregationFunctionSymbol {

    private final RDFDatatype xsdStringType;
    private final boolean isDistinct;

    protected GroupConcatSPARQLFunctionSymbolImpl(RDFDatatype xsdStringType, int arity, boolean isDistinct) {
        super(
                "SP_GROUP_CONCAT"+arity,
                SPARQL.GROUP_CONCAT,
                IntStream.range(0, arity).boxed()
                        .map(i -> xsdStringType)
                        .collect(ImmutableCollectors.toList())
        );
        this.xsdStringType = xsdStringType;
        this.isDistinct = isDistinct;
        if(arity < 1 || arity > 2){
            throw new RuntimeException("Arity 1 or 2 expected");
        }
    }

    public boolean isDistinct() {
        return isDistinct;
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(xsdStringType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public boolean isNullable(ImmutableSet<Integer> nullableIndexes) {
        return true;
    }

    @Override
    public boolean isAggregation() {
        return true;
    }

    @Override
    public Optional<AggregationSimplification> decomposeIntoDBAggregation(
            ImmutableList<? extends ImmutableTerm> subTerms, ImmutableList<ImmutableSet<RDFTermType>> possibleRDFTypes,
            boolean hasGroupBy, VariableNullability variableNullability, VariableGenerator variableGenerator, TermFactory termFactory) {

        if (possibleRDFTypes.size() != getArity()) {
            throw new IllegalArgumentException("The size of possibleRDFTypes is expected to match the arity of " +
                    "the function symbol");
        }
        /*
         * Currently, we can assume that the second argument is a constant string (enforced by the translator)
         */
        ImmutableTerm firstSubTerm = subTerms.get(0);
        ImmutableSet<RDFTermType> subTermPossibleTypes = possibleRDFTypes.get(0);

        String separator = (getArity() == 2)
                ? Optional.of(subTerms.get(1))
                .filter(t -> t instanceof RDFConstant)
                .map(t -> (RDFConstant)t)
                .filter(c -> c.getType().isA(xsdStringType))
                .map(Constant::getValue)
                .orElseThrow(() -> new MinorOntopInternalBugException("Was expected to be a constant string"))
                : " ";

        return subTermPossibleTypes.stream().allMatch(t -> t.isA(xsdStringType))
                ? Optional.of(decomposeString(firstSubTerm, separator, hasGroupBy, variableNullability,
                    variableGenerator, termFactory))
                : Optional.of(decomposeWithNonString(firstSubTerm, separator, hasGroupBy, variableNullability,
                    variableGenerator, termFactory));
    }

    private AggregationSimplification decomposeString(ImmutableTerm firstSubTerm, String separator, boolean hasGroupBy,
                                                      VariableNullability variableNullability,
                                                      VariableGenerator variableGenerator, TermFactory termFactory) {
        ImmutableTerm subTermLexicalTerm = extractLexicalTerm(firstSubTerm, termFactory);

        ImmutableFunctionalTerm dbAggTerm = createAggregate(subTermLexicalTerm, separator, termFactory);

        Variable dbAggregationVariable = variableGenerator.generateNewVariable("str");

        boolean isSubTermNullable = subTermLexicalTerm.isNullable(variableNullability.getNullableVariables());
        DBConstant emptyString = termFactory.getDBStringConstant("");

        // If DB group concat returns a NULL, replaces it by ""
        boolean dbAggMayReturnNull = !(hasGroupBy && (!isSubTermNullable));
        ImmutableTerm nonNullDBAggregate = dbAggMayReturnNull
                ? termFactory.getDBCoalesce(dbAggregationVariable, emptyString)
                : dbAggregationVariable;

        ImmutableFunctionalTerm liftedTerm = termFactory.getRDFFunctionalTerm(
                nonNullDBAggregate,
                termFactory.getRDFTermTypeConstant(xsdStringType));

        ImmutableFunctionalTerm.FunctionalTermDecomposition decomposition = termFactory.getFunctionalTermDecomposition(
                liftedTerm,
                ImmutableMap.of(dbAggregationVariable, dbAggTerm));

        return AggregationSimplification.create(decomposition);
    }

    private ImmutableFunctionalTerm createAggregate(ImmutableTerm subTermLexicalTerm, String separator, TermFactory termFactory) {
        return termFactory.getDBGroupConcat(subTermLexicalTerm, separator, isDistinct);
    }

    private AggregationSimplification decomposeWithNonString(ImmutableTerm subTerm, String separator, boolean hasGroupBy,
                                                             VariableNullability variableNullability,
                                                             VariableGenerator variableGenerator, TermFactory termFactory) {
        throw new RuntimeException("TODO: support the case with non-strings");
    }

    @Override
    public Constant evaluateEmptyBag(TermFactory termFactory) {
        return termFactory.getRDFLiteralConstant("", xsdStringType);
    }
}
