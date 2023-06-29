package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

@Singleton
public class OracleSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer {

    @Inject
    private OracleSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            protected String serializeDBConstant(DBConstant constant) {
                DBTermType dbType = constant.getType();
                switch (dbType.getCategory()) {
                    case DATETIME:
                        return String.format("TIMESTAMP '%s'", constant.getValue());
                    default:
                        return super.serializeDBConstant(constant);
                }
            }
        });
    }

    public static final int NAME_MAX_LENGTH = 30;
    public static final int NAME_NUMBER_LENGTH = 3;

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {

            @Override
            protected AttributeAliasFactory createAttributeAliasFactory() {
                return new LimitLengthAttributeAliasFactory(idFactory, NAME_MAX_LENGTH, NAME_NUMBER_LENGTH);
            }

            @Override
            protected String serializeDummyTable() {
                return "FROM dual";
            }

            /**
             * Versions < 12.1 are not supported
             * Reason: In 12.1 and later, you can use the OFFSET and/or FETCH [FIRST | NEXT] operators
             */
            @Override
            protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
            }

            @Override
            protected String serializeLimit(long limit, boolean noSortCondition) {
                // ROWNUM <= limit could also be used
                return String.format("FETCH NEXT %d ROWS ONLY", limit);
            }

            @Override
            protected String serializeOffset(long offset, boolean noSortCondition) {
                return String.format("OFFSET %d ROWS\nFETCH NEXT 99999999 ROWS ONLY", offset);
            }

            @Override
            protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                /* We build the query string of the form
                *  `SELECT <variables> FROM <subquery> CROSS JOIN JSON_TABLE(<flattenedVar>, '$[*]' COLUMNS (<outputVar> VARCHAR2(1000) FORMAT JSON PATH '$' [, <indexVar> FOR ORDINALITY]))
                */
                StringBuilder builder = new StringBuilder();

                builder.append(
                        String.format(
                                "%s CROSS JOIN JSON_TABLE(%s, '$[*]' COLUMNS(%s VARCHAR2(1000) FORMAT JSON PATH '$'",
                                subQuerySerialization.getString(),
                                allColumnIDs.get(flattenedVar).getSQLRendering(),
                                allColumnIDs.get(outputVar).getSQLRendering()
                        ));
                indexVar.ifPresent( v -> builder.append(String.format(", %s FOR ORDINALITY", allColumnIDs.get(v).getSQLRendering())));
                builder.append(String.format(")) %s", generateFreshViewAlias().getSQLRendering()));

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
