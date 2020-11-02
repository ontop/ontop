package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;

import javax.annotation.Nonnull;
import java.util.Objects;

public class DremioQuotedIDFactory extends SQLStandardQuotedIDFactory {

    @Override
    protected QuotedID createFromString(@Nonnull String s) {
        Objects.requireNonNull(s);
        
        if (s.startsWith(QUOTATION_STRING) && s.endsWith(QUOTATION_STRING))
            return new QuotedIDImpl(s.substring(1, s.length() - 1), QUOTATION_STRING);

        return new QuotedIDImpl(s, QUOTATION_STRING, false);
    }

    @Override
    public String getIDQuotationString() {
        return QUOTATION_STRING;
    }
}
