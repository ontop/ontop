package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;

import javax.annotation.Nonnull;
import java.util.Objects;

public class DremioQuotedIDFactory extends SQLStandardQuotedIDFactory {
    @Override
    protected QuotedID createFromString(@Nonnull String s) {
        Objects.requireNonNull(s);
        return new QuotedIDImpl(s, NO_QUOTATION, false);
    }

    @Override
    public String getIDQuotationString() {
        return NO_QUOTATION;
    }
}
