package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Objects;

/**
 * Creates QuotedIDs from database records
 * (any quotation marks are actually part of the identifier)
 *
 * TO BE USED ONLY IN METADATA EXTRACTION
 */
@NonNullByDefault
public class RawQuotedIDFactory extends SQLStandardQuotedIDFactory {

    private final QuotedIDFactory idFactory;

    public RawQuotedIDFactory(QuotedIDFactory idFactory) {
        this.idFactory = Objects.requireNonNull(idFactory);
    }

    /**
     * Creates an ID from the database record (as though it is a quoted name).
     *
     * @param s the name
     * @return quoted name
     */
    protected QuotedID createFromString(String s) {
        Objects.requireNonNull(s);
        return new QuotedIDImpl(s, idFactory.getIDQuotationString(), true);
    }

    @Override
    public String getIDQuotationString() {
        return idFactory.getIDQuotationString();
    }

    @Override
    public String getIDFactoryType() {
        // For completeness. Apparently lookup by type ID is not relevant for this kind of factory
        return "RAW-" + idFactory.getIDFactoryType();
    }

}
