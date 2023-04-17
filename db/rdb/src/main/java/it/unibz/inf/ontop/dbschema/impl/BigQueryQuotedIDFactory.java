package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory.IDFactoryType;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Objects;

@IDFactoryType("BIGQUERY")
@NonNullByDefault
public class BigQueryQuotedIDFactory extends SQLStandardQuotedIDFactory {

    private static final String SQL_QUOTATION_STRING = "`";

    @Override
    protected QuotedID createFromString(String s) {
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
