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
import it.unibz.inf.ontop.model.term.Variable;

import java.util.stream.Collectors;

@Singleton
public class MySQLSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private MySQLSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer,
                                           SQLDialectAdapter dialectAdapter) {
        super(sqlTermSerializer, dialectAdapter);
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSQLRelationVisitingSerializer(sqlTermSerializer, dialectAdapter, dbParameters.getQuotedIDFactory()) {

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
                });
    }



}
