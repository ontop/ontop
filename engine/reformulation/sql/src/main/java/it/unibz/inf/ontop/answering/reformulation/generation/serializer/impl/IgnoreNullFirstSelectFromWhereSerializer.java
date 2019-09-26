package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLOrderComparator;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.stream.Collectors;

/**
 * Useful for instead for SQL Server which already treats NULLs as the lowest values
 * Therefore it follows the semantics of  (ASC + NULLS FIRST) and (DESC + NULLS LAST)
 */
@Singleton
public class IgnoreNullFirstSelectFromWhereSerializer implements SelectFromWhereSerializer {

    private final SQLTermSerializer sqlTermSerializer;
    private final SQLDialectAdapter dialectAdapter;

    @Inject
    private IgnoreNullFirstSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer,
                                                     SQLDialectAdapter dialectAdapter) {
        this.sqlTermSerializer = sqlTermSerializer;
        this.dialectAdapter = dialectAdapter;
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new SQLServerSQLRelationVisitingSerializer(sqlTermSerializer, dialectAdapter, dbParameters.getQuotedIDFactory()));
    }


    protected static class SQLServerSQLRelationVisitingSerializer extends DefaultSelectFromWhereSerializer.DefaultSQLRelationVisitingSerializer {

        protected SQLServerSQLRelationVisitingSerializer(SQLTermSerializer sqlTermSerializer,
                                                         SQLDialectAdapter dialectAdapter, QuotedIDFactory idFactory) {
            super(sqlTermSerializer, dialectAdapter, idFactory);
        }

        @Override
        protected String serializeOrderBy(ImmutableList<SQLOrderComparator> sortConditions,
                                          ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
            if (sortConditions.isEmpty())
                return "";

            String conditionString = sortConditions.stream()
                    .map(c -> sqlTermSerializer.serialize(c.getTerm(), fromColumnMap))
                    .collect(Collectors.joining(", "));

            return String.format("ORDER BY %s\n", conditionString);
        }
    }



}
