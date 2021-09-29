package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.model.term.TermFactory;

@Singleton
public class SapHanaSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private SapHanaSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory));
    }

    @Override
    public SelectFromWhereSerializer.QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSelectFromWhereSerializer.DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    /**
                     * https://help.sap.com/viewer/4fe29514fd584807ac9f2a04f6754767/2.0.03/en-US/20fcf24075191014a89e9dc7b8408b26.html
                     *
                     * LIMIT
                     * Limits the number of records returned and behaves like TOP.
                     * <limit> ::=
                     *  LIMIT <unsigned_integer> [ OFFSET <unsigned_integer> ]
                     *
                     * The following example returns the first 3 records after skipping 5 records.
                     * LIMIT 3 [OFFSET 5]
                     *
                     */

                    // serializeLimit is standard

                    @Override
                    protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                        return String.format("LIMIT %d\nOFFSET %d", limit, offset);
                    }

                    @Override
                    protected String serializeOffset(long offset, boolean noSortCondition) {
                        return serializeLimitOffset(Integer.MAX_VALUE, offset, noSortCondition);
                    }
                });
    }
}


