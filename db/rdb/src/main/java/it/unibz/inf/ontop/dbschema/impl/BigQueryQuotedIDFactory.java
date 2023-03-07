package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BigQueryQuotedIDFactory extends SQLStandardQuotedIDFactory {

    private static final String SQL_QUOTATION_STRING = "`";

    @Override
    protected QuotedID createFromString(@Nonnull String s) {
        Objects.requireNonNull(s);

        if (s.startsWith(SQL_QUOTATION_STRING) && s.endsWith(SQL_QUOTATION_STRING))
            return new QuotedIDImpl(s.substring(1, s.length() - 1), SQL_QUOTATION_STRING, true);

        return new QuotedIDImpl(s, SQL_QUOTATION_STRING, true);
    }

    @Override
    public String getIDQuotationString() {
        return SQL_QUOTATION_STRING;
    }
}
