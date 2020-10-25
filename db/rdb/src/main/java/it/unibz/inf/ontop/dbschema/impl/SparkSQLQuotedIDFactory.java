package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;

/**
 * Creates QuotedIdentifiers following the rules of MySQL:<br>
 *    - unquoted table identifiers are preserved<br>
 *    - unquoted column identifiers are not case-sensitive<br>
 *    - quoted identifiers are preserved
 */

public class SparkSQLQuotedIDFactory implements QuotedIDFactory {

    private static final String MY_SQL_QUOTATION_STRING = "`";
    private final boolean caseSensitiveTableNames;

    SparkSQLQuotedIDFactory(boolean caseSensitiveTableNames) {
        this.caseSensitiveTableNames = caseSensitiveTableNames;
    }

    @Override
    public QuotedID createAttributeID(String s) {
        if (s == null)
            return new QuotedIDImpl(s, SQLStandardQuotedIDFactory.NO_QUOTATION);

        if (s.startsWith(MY_SQL_QUOTATION_STRING) && s.endsWith(MY_SQL_QUOTATION_STRING))
            return new QuotedIDImpl(s.substring(1, s.length() - 1), MY_SQL_QUOTATION_STRING, false);

        if (s.startsWith(SQLStandardQuotedIDFactory.QUOTATION_STRING) && s.endsWith(SQLStandardQuotedIDFactory.QUOTATION_STRING))
            return new QuotedIDImpl(s.substring(1, s.length() - 1), MY_SQL_QUOTATION_STRING, false);

        return new QuotedIDImpl(s, SQLStandardQuotedIDFactory.NO_QUOTATION, false);
    }

    @Override
    public RelationID createRelationID(String schema, String table) {
        return new RelationIDImpl(createFromString(schema), createFromString(table));
    }

    private QuotedID createFromString(String s) {
        if (s == null)
            return new QuotedIDImpl(s, SQLStandardQuotedIDFactory.NO_QUOTATION);

        if (s.startsWith("`") && s.endsWith(MY_SQL_QUOTATION_STRING))
            return new QuotedIDImpl(s.substring(1, s.length() - 1), MY_SQL_QUOTATION_STRING, caseSensitiveTableNames);

        if (s.startsWith("\"") && s.endsWith(SQLStandardQuotedIDFactory.QUOTATION_STRING))
            return new QuotedIDImpl(s.substring(1, s.length() - 1), MY_SQL_QUOTATION_STRING, caseSensitiveTableNames);

        return new QuotedIDImpl(s, SQLStandardQuotedIDFactory.NO_QUOTATION, caseSensitiveTableNames);
    }

    @Override
    public String getIDQuotationString() {
        return MY_SQL_QUOTATION_STRING;
    }
}

