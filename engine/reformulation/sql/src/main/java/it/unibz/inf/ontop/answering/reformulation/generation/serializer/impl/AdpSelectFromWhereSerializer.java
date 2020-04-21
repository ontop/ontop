package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.model.term.TermFactory;

@Singleton
public class AdpSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private AdpSelectFromWhereSerializer(TermFactory termFactory,
                                         SQLDialectAdapter dialectAdapter) {
        super(new DefaultSQLTermSerializer(termFactory), dialectAdapter);
    }

    @Override
    public SelectFromWhereSerializer.QuerySerialization serialize(SelectFromWhereWithModifiers
                                                                          selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSelectFromWhereSerializer.DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    /**
                     * same as PostgreSQL
                     * serializeLimit is standard
                     */
                    @Override
                    protected String serializeOffset(long offset) {
                        return String.format("LIMIT ALL\nOFFSET %d", offset);
                    }

                    @Override
                    protected String serializeLimitOffset(long limit, long offset) {
                        return String.format("LIMIT %d\nOFFSET %d", limit, offset);
                    }
                });
    }
}

