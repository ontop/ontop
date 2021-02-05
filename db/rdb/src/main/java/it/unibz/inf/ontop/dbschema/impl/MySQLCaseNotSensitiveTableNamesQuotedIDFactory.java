package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;

import javax.annotation.Nonnull;

public class MySQLCaseNotSensitiveTableNamesQuotedIDFactory extends MySQLAbstractQuotedIDFactory {
    @Override
    public QuotedID createAttributeID(@Nonnull String s) {
        return createFromString(s, false);
    }

    @Override
    protected QuotedID createFromString(@Nonnull String s) {
        return createFromString(s, false);
    }
}
