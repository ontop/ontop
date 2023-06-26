package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SQLOneTupleDummyQueryExpression;
import it.unibz.inf.ontop.generation.algebra.SQLOrderComparator;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;


@Singleton
public class SQLServerSelectFromWhereSerializer extends IgnoreNullFirstSelectFromWhereSerializer {

    @Inject
    private SQLServerSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory));
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(new IgnoreNullFirstRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {

            //@Override SQL Server allows no FROM clause
            //protected String serializeDummyTable() {
            //    // "\"example\"" from SQLAdapter
            //    return "";
            //}

            /**
             *  https://docs.microsoft.com/en-us/sql/t-sql/queries/select-order-by-clause-transact-sql?view=sql-server-ver15
             *
             * <offset_fetch> ::=
             * {
             *     OFFSET { integer_constant | offset_row_count_expression } { ROW | ROWS }
             *     [
             *       FETCH { FIRST | NEXT } {integer_constant | fetch_row_count_expression } { ROW | ROWS } ONLY
             *     ]
             * }
             */

            @Override
            protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
            }

            @Override
            protected String serializeLimit(long limit, boolean noSortCondition) {
                return String.format("OFFSET 0 ROWS\nFETCH NEXT %d ROWS ONLY", limit);
            }

            @Override
            protected String serializeOffset(long offset, boolean noSortCondition) {
                return String.format("OFFSET %d ROWS", offset);
            }

            @Override
            protected String serializeOrderBy(ImmutableList<SQLOrderComparator> sortConditions, ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap, boolean hasOffsetOrLimit) {
                return String.format("%s%s",
                        super.serializeOrderBy(sortConditions, fromColumnMap),
                        hasOffsetOrLimit || sortConditions.isEmpty() ? "" : " OFFSET 0 ROWS\n");
            }

            @Override
            public QuerySerialization visit(SQLOneTupleDummyQueryExpression sqlOneTupleDummyQueryExpression) {
                String fromString = serializeDummyTable();
                String sqlSubString = String.format("(SELECT 1 AS dummyVarSQLServer %s) tdummy", fromString);
                return new QuerySerializationImpl(sqlSubString, ImmutableMap.of());
            }

            @Override
            protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                if(indexVar.isPresent()) {
                    /*
                    * adding `<indexVar> int '$.sql:identity()'` to the `WITH` clause can create a position argument, but
                    * this feature is only supported in the "serverless SQL pool in Synapse Analytics".
                    */
                    throw new SQLSerializationException("SQLServer currently does not support FLATTEN with position arguments.");
                }

                //We build the query string of the form SELECT <variables> FROM <subquery> CROSS APPLY OPENJSON(<flattenedVariable>) WITH (<names> NVARCHAR(MAX) '$')
                StringBuilder builder = new StringBuilder();

                /*
                 * When flattening an array, we have to indicate if the children are either a SCALAR value or a NESTED value.
                 * Since we cannot know that in advance, we instead create 2 children: `outputjson` and `outputscalar`.
                 * The incorrect one will be NULL, so we can then use a `CASE WHEN ...` over it to rename the correct
                 * one to the output variable.
                 */
                var attributeAliasFactory = createAttributeAliasFactory();
                String jsonVariable = attributeAliasFactory.createAttributeAlias(outputVar.getName() + "json").getSQLRendering();
                String scalarVariable = attributeAliasFactory.createAttributeAlias(outputVar.getName() + "scalar").getSQLRendering();

                builder.append(
                        String.format(
                                "%s CROSS APPLY (SELECT (CASE WHEN %s IS NOT NULL THEN %S ELSE %S END) as %s FROM OPENJSON(%s) WITH (%s NVARCHAR(MAX) '$', %s NVARCHAR(MAX) '$' AS JSON)) %s",
                                subQuerySerialization.getString(),
                                jsonVariable,
                                jsonVariable,
                                scalarVariable,
                                allColumnIDs.get(outputVar).getSQLRendering(),
                                allColumnIDs.get(flattenedVar).getSQLRendering(),
                                scalarVariable,
                                jsonVariable,
                                generateFreshViewAlias().getSQLRendering()
                        ));

                return new QuerySerializationImpl(
                        builder.toString(),
                        allColumnIDs.entrySet().stream()
                                .filter(e -> e.getKey() != flattenedVar)
                                .collect(ImmutableCollectors.toMap())
                );
            }
        });
    }
}
