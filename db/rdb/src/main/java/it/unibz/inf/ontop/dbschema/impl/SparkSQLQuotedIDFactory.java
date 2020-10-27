package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;

/**
 * Creates QuotedIdentifiers following the rules of SparkSQL:
 *    - double and single quotes are not tolerated only for schema and attributes definition
 */

public class SparkSQLQuotedIDFactory implements QuotedIDFactory {

    private static final String SINGLE_QUOTATION_STRING = "'";
    private static final String SQL_QUOTATION_STRING = "`";
    private final boolean caseSensitiveTableNames;

    SparkSQLQuotedIDFactory(boolean caseSensitiveTableNames) {
        this.caseSensitiveTableNames = caseSensitiveTableNames;
    }

    @Override
    public QuotedID createAttributeID(String s) {
        return createFromString(s);
    }

    @Override
    public RelationID createRelationID(String schema, String table) {
        return new RelationIDImpl(createFromString(schema), createFromString(table));
    }

    /**
     *
     * @param s schema/table/attribute string name possibly quoted (SQL rendering)
     * @return
     */
    private QuotedID createFromString(String s) {
        if (s == null)
            return new QuotedIDImpl(s, SQLStandardQuotedIDFactory.NO_QUOTATION);

        // Backticks are tolerated for SparkSQL schema and table names, but not necessary
        if (s.startsWith(SQL_QUOTATION_STRING) && s.endsWith(SQL_QUOTATION_STRING))
            return new QuotedIDImpl(s.substring(1, s.length() - 1), SQL_QUOTATION_STRING, caseSensitiveTableNames);

        return new QuotedIDImpl(s, SQLStandardQuotedIDFactory.NO_QUOTATION, caseSensitiveTableNames);
    }

    @Override
    public String getIDQuotationString() {
        return SQL_QUOTATION_STRING;
    }
}

