package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory.IDFactoryType;
import org.eclipse.jdt.annotation.NonNullByDefault;

@IDFactoryType("BIGQUERY")
@NonNullByDefault
public class BigQueryQuotedIDFactory extends AbstractBacktickQuotedIDFactory {

    @Override
    protected QuotedID createFromString(String s) {
        return createFromString(s, BACKTICK_QUOTATION_STRING, i -> i, BACKTICK_QUOTATION_STRING, true);
    }

}
