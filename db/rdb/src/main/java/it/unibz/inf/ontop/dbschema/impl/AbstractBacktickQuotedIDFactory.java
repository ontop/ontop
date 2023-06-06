package it.unibz.inf.ontop.dbschema.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AbstractBacktickQuotedIDFactory extends SQLStandardQuotedIDFactory {

    protected static final String BACKTICK_QUOTATION_STRING = "`";

    @Override
    public String getIDQuotationString() {
        return BACKTICK_QUOTATION_STRING;
    }

}
