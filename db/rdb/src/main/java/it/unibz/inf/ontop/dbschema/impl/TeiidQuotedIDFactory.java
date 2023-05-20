package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory.IDFactoryType;
import org.eclipse.jdt.annotation.NonNullByDefault;

@IDFactoryType("TEIID")
@NonNullByDefault
public class TeiidQuotedIDFactory extends SQLStandardQuotedIDFactory {

    @Override
    protected QuotedID createFromString(String s) {
        return createFromString(s, QUOTATION_STRING, i -> i, NO_QUOTATION, false);
    }

}
