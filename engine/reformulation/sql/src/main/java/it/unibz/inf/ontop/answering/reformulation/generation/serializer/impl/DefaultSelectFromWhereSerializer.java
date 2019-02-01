package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLSerializedQuery;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhere;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;

@Singleton
public class DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private DefaultSelectFromWhereSerializer() {
    }

    @Override
    public String serialize(SelectFromWhere selectFromWhere) {
        return selectFromWhere.acceptVisitor(
                new DefaultSQLRelationVisitingSerializer());
    }


    /**
     * Mutable: one instance per SQL query to generate
     */
    protected static class DefaultSQLRelationVisitingSerializer implements SQLRelationVisitor<String> {

        @Override
        public String visit(SelectFromWhere selectFromWhere) {
            throw new RuntimeException("TODO: implement the serialization of a SelectFromWhere");
        }

        @Override
        public String visit(SQLSerializedQuery sqlSerializedQuery) {
            return sqlSerializedQuery.getSQLString();
        }
    }


}
