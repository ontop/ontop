package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;

import javax.annotation.Nonnull;
import java.util.Objects;

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
     * @param attributeId
     * @return
     */
    @Override
    public QuotedID createAttributeID(@Nonnull String attributeId) {
        // ID is as though it is quoted -- DB stores names as is
        return new QuotedIDImpl(attributeId, idFactory.getIDQuotationString());
    }

    /**
     * creates relation id from the database record (as though it is quoted)
     *
     * @param components as is in DB (possibly null)
     * @return
     */

    @Override
    public RelationID createRelationID(String... components) {
        // IDs are as though they are quoted -- DB stores names as is
        Objects.requireNonNull(components[components.length - 1]);
        ImmutableList.Builder<QuotedID> builder = ImmutableList.builder();
        for (int i = components.length - 1; i >= 0; i--)
            if (components[i] != null)
                builder.add(new QuotedIDImpl(components[i], idFactory.getIDQuotationString()));

        return new RelationIDImpl(builder.build());
    }

    @Override
    public String getIDQuotationString() {
        return idFactory.getIDQuotationString();
    }
}
