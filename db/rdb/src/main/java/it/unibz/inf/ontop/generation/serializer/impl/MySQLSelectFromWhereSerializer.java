package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SQLOrderComparator;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.StringUtils;

import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class MySQLSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    private static final ImmutableMap<Character, String> BACKSLASH = ImmutableMap.of('\\', "\\\\");

    @Inject
    protected MySQLSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            protected String serializeStringConstant(String constant) {
                // parent method + doubles backslashes
                return StringUtils.encode(super.serializeStringConstant(constant), BACKSLASH);
            }
        });
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {

                    /**
                     * MySQL seems to already treat NULLs as the lowest values
                     * Therefore it seems to follow the semantics of  (ASC + NULLS FIRST) and (DESC + NULLS LAST)
                     *
                     * See http://sqlfiddle.com/#!9/255d2e/18
                     */
                    @Override
                    protected String serializeOrderBy(ImmutableList<SQLOrderComparator> sortConditions,
                                                      ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
                        if (sortConditions.isEmpty())
                            return "";

                        String conditionString = sortConditions.stream()
                                .map(c -> sqlTermSerializer.serialize(c.getTerm(), fromColumnMap) +
                                        (c.isAscending() ? "" : " DESC"))
                                .collect(Collectors.joining(", "));

                        return String.format("ORDER BY %s\n", conditionString);
                    }

                    /**
                     *  http://dev.mysql.com/doc/refman/5.0/en/select.html
                     *
                     * With two arguments, the first argument specifies the offset of the first row to return,
                     * and the second specifies the maximum number of rows to return. The offset of the initial
                     * row is 0 (not 1):
                     * SELECT * FROM tbl LIMIT 5,10;  # Retrieve rows 6-15
                     *
                     * To retrieve all rows from a certain offset up to the end of the result set, you can
                     * use some large number for the second parameter. This statement retrieves all rows from
                     * the 96th row to the last:
                     * SELECT * FROM tbl LIMIT 95,18446744073709551615;
                     *
                     * With one argument, the value specifies the number of rows to return from the beginning
                     * of the result set:
                     * SELECT * FROM tbl LIMIT 5;     # Retrieve first 5 rows
                     * In other words, LIMIT row_count is equivalent to LIMIT 0, row_count.
                     */

                    // serializeLimitOffset and serializeLimit are standard

                    @Override
                    protected String serializeOffset(long offset, boolean noSortCondition) {
                        return serializeLimitOffset(Long.MAX_VALUE, offset, noSortCondition);
                    }

                    /**
                     * MySQL: requires parenthesis for complex mix of JOIN/LEFT JOIN (observed for v5.7)
                     */
                    @Override
                    protected String formatBinaryJoin(String operatorString, QuerySerialization left, QuerySerialization right, String onString) {
                        return String.format("(%s\n %s \n%s %s)", left.getString(), operatorString, right.getString(), onString);
                    }

                    @Override
                    protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                        /* We build the query string of the form
                        /  SELECT <variables> FROM <subquery> CROSS JOIN JSON_TABLE(<flattenedVariable>, '$[*]',
                        /       COLUMNS (<outputVar> JSON path '$' [, <indexVar> for ordinality]))
                        */
                        StringBuilder builder = new StringBuilder();

                        builder.append(
                                String.format(
                                        getFlattenFunctionFormat(),
                                        subQuerySerialization.getString(),
                                        allColumnIDs.get(flattenedVar).getSQLRendering(),
                                        allColumnIDs.get(outputVar).getSQLRendering()

                                ));
                        indexVar.ifPresent(v -> builder.append(String.format(", %s for ordinality", allColumnIDs.get(v).getSQLRendering())));
                        builder.append(")) ");
                        builder.append(generateFreshViewAlias().getSQLRendering());

                        return new QuerySerializationImpl(
                                builder.toString(),
                                allColumnIDs.entrySet().stream()
                                        .filter(e -> e.getKey() != flattenedVar)
                                        .collect(ImmutableCollectors.toMap())
                        );
                    }
                });
    }

    protected String getFlattenFunctionFormat() {
        /*
        *   By default, running JSON_TABLE on a JSON-array that was created by a different JSON_TABLE call
        *   will return an empty list. We can circumvent this, by putting another array around it (calling
        *   `JSON_ARRAY`) and then de-referencing it again in the path selector ($[0][*]).
         */
        return "%s CROSS JOIN JSON_TABLE(JSON_ARRAY(%s), '$[0][*]' columns(%s JSON path '$'";
    }
}
