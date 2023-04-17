package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory.IDFactoryType;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Objects;

@IDFactoryType("TEIID")
@NonNullByDefault
public class TeiidQuotedIDFactory extends SQLStandardQuotedIDFactory {

    @Override
    protected QuotedID createFromString(String s) {
        Objects.requireNonNull(s);

        if (s.startsWith(QUOTATION_STRING) && s.endsWith(QUOTATION_STRING))
            return new QuotedIDImpl(s.substring(1, s.length() - 1), QUOTATION_STRING, false);

        return new QuotedIDImpl(s, NO_QUOTATION, false);
    }

}
