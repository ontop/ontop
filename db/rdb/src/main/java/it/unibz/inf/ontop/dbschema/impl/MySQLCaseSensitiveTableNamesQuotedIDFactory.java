package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory.IDFactoryType;
import org.eclipse.jdt.annotation.NonNullByDefault;

@IDFactoryType("MYSQL-U")
@NonNullByDefault
public class MySQLCaseSensitiveTableNamesQuotedIDFactory extends MySQLAbstractQuotedIDFactory {

    @Override
    public QuotedID createAttributeID(String s) {
        return createFromString(s, false);
    }

    @Override
    protected QuotedID createFromString(String s) {
        return createFromString(s, true);
    }

}
