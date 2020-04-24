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

@Singleton
public class DenodoSelectFromWhereSerializer implements SelectFromWhereSerializer {

    private final SQLTermSerializer sqlTermSerializer;
    private final SQLDialectAdapter dialectAdapter;

    @Inject
    private DenodoSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer,
                                            SQLDialectAdapter dialectAdapter) {
        this.sqlTermSerializer = sqlTermSerializer;
        this.dialectAdapter = dialectAdapter;
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DenodoSelectFromWhereSerializer.DenodoRelationVisitingSerializer(sqlTermSerializer, dialectAdapter, dbParameters.getQuotedIDFactory()));
    }


    protected static class DenodoRelationVisitingSerializer extends DefaultSelectFromWhereSerializer.DefaultSQLRelationVisitingSerializer {

        private final String CONTEXT = " CONTEXT('i18n'='us_utc_iso')";

        protected DenodoRelationVisitingSerializer(SQLTermSerializer sqlTermSerializer,
                                                   SQLDialectAdapter dialectAdapter, QuotedIDFactory idFactory) {
            super(sqlTermSerializer, dialectAdapter, idFactory);
        }

        /**
         * Denodo cannot parse:
         * ORDER BY xxx NULLS FIRST
         * When using NULLS FIRST/NULLS LAST, the order should be explicit, e.g.:
         * ORDER BY ASC xxx NULLS FIRST
         */
        @Override
        protected String serializeOrderBy(ImmutableList<SQLOrderComparator> sortConditions,
                                          ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
            if (sortConditions.isEmpty())
                return "";

            String conditionString = sortConditions.stream()
                    .map(c -> sqlTermSerializer.serialize(c.getTerm(), fromColumnMap) +
                            (c.isAscending() ? " ASC NULLS FIRST" : " DESC NULLS LAST"))
                    .collect(Collectors.joining(", "));

            return String.format("ORDER BY %s\n", conditionString);
        }

        /**
         * Adds a CONTEXT clause, so that the local time zone is set to uct
         * see https://community.denodo.com/docs/html/browse/6.0/vdp/vql/queries_select_statement/context_clause/context_clause
         */
//        @Override
//        public QuerySerialization visit(SelectFromWhereWithModifiers selectFromWhere) {
//            QuerySerialization qs = super.visit(selectFromWhere);
//            return new DefaultSelectFromWhereSerializer.QuerySerializationImpl(
//                    qs.getString() + CONTEXT,
//                    qs.getColumnIDs()
//            );
//        }
    }

}
