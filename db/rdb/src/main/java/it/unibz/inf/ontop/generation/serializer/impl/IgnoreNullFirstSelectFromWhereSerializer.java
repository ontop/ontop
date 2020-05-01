package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.algebra.SQLOrderComparator;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
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
public class IgnoreNullFirstSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    protected IgnoreNullFirstSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer) {
        super(sqlTermSerializer);
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(new IgnoreNullFirstRelationVisitingSerializer(dbParameters.getQuotedIDFactory()));
    }

    protected class IgnoreNullFirstRelationVisitingSerializer extends DefaultRelationVisitingSerializer {

        protected IgnoreNullFirstRelationVisitingSerializer(QuotedIDFactory idFactory) { super(idFactory); }

        @Override
        protected String serializeOrderBy(ImmutableList<SQLOrderComparator> sortConditions,
                ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
            if (sortConditions.isEmpty())
                return "";

            String conditionString = sortConditions.stream()
                    .map(c -> sqlTermSerializer.serialize(c.getTerm(), fromColumnMap)
                            + (c.isAscending() ? "" : " DESC"))
                    .collect(Collectors.joining(", "));

            return String.format("ORDER BY %s\n", conditionString);
        }
    }
}
