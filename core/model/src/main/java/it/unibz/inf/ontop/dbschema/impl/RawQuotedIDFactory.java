package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;

/**
 * Creates QuotedIDs from database records
 * (any quotation marks are actually part of the identifier)
 *
 * TO BE USED ONLY IN METADATA EXTRACTION
 */

public class RawQuotedIDFactory implements QuotedIDFactory {

    private final QuotedIDFactory idFactory;

    public RawQuotedIDFactory(QuotedIDFactory idFactory) {
        this.idFactory = idFactory;
    }

    /**
     * creates attribute ID from the database record (as though it is a quoted name)
     *
     * @param s
     * @return
     */

    @Override
    public QuotedID createAttributeID(String s) {
        // ID is as though it is quoted -- DB stores names as is
        return new QuotedIDImpl(s, idFactory.getIDQuotationString());
    }

    /**
     * creates relation id from the database record (as though it is quoted)
     *
     * @param schema as is in DB (possibly null)
     * @param table as is in DB
     * @return
     */

    @Override
    public RelationID createRelationID(String schema, String table) {
        // both IDs are as though they are quoted -- DB stores names as is
        return new RelationIDImpl(schema != null
            ? ImmutableList.of(new QuotedIDImpl(table, idFactory.getIDQuotationString()),
                new QuotedIDImpl(schema, idFactory.getIDQuotationString()))
            : ImmutableList.of(new QuotedIDImpl(table, idFactory.getIDQuotationString())));
    }

    @Override
    public String getIDQuotationString() {
        return idFactory.getIDQuotationString();
    }
}
