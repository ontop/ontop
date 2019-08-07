package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.stream.Collectors;

public class Serializers {

    private static final String FUNCTIONAL_TEMPLATE = "%s(%s)";
    private static final String FUNCTIONAL_DISTINCT_TEMPLATE = "%s(DISTINCT(%s))";
    private final static String IN_BRACKETS_TEMPLATE = "(%s)";
    private final static String CAST_TEMPLATE = "CAST(%s AS %s)";

    public static DBFunctionSymbolSerializer getRegularSerializer(String nameInDialect) {
        return (terms, termConverter, termFactory) -> {
            String parameterString = terms.stream()
                    .map(termConverter)
                    .collect(Collectors.joining(","));
            return String.format(FUNCTIONAL_TEMPLATE, nameInDialect, parameterString);
        };
    }

    public static DBFunctionSymbolSerializer getDistinctAggregationSerializer(String nameInDialect) {
        return (terms, termConverter, termFactory) -> {
            String parameterString = terms.stream()
                    .map(termConverter)
                    .collect(Collectors.joining(","));
            return String.format(FUNCTIONAL_DISTINCT_TEMPLATE, nameInDialect, parameterString);
        };
    }

    public static DBFunctionSymbolSerializer getOperatorSerializer(String operator) {
        String separator = String.format(" %s ", operator);

        return (terms, termConverter, termFactory) -> {
            String expression = terms.stream()
                    .map(termConverter)
                    .collect(Collectors.joining(separator));
            return String.format(IN_BRACKETS_TEMPLATE, expression);
        };
    }

    public static DBFunctionSymbolSerializer getCastSerializer(DBTermType targetType) {
        return (terms, termConverter, termFactory) -> String.format(
                CAST_TEMPLATE, termConverter.apply(terms.get(0)), targetType.getCastName());
    }
}
