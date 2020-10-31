package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Creates QuotedIDs from database records
 * (any quotation marks are actually part of the identifier)
 *
 * TO BE USED ONLY IN METADATA EXTRACTION
 */

public class RawQuotedIDFactory extends SQLStandardQuotedIDFactory {

    private final QuotedIDFactory idFactory;

    public RawQuotedIDFactory(QuotedIDFactory idFactory) {
        this.idFactory = idFactory;
    }

    /**
     * creates an ID from the database record (as though it is a quoted name)
     *
     * @param s
     * @return
     */
    protected QuotedID createFromString(@Nonnull String s) {
        Objects.requireNonNull(s);
        return new QuotedIDImpl(s, idFactory.getIDQuotationString());
    }

    @Override
    public String getIDQuotationString() {
        return idFactory.getIDQuotationString();
    }
}
