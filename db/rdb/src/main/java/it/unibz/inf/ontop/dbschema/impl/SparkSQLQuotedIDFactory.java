package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory.IDFactoryType;
import it.unibz.inf.ontop.dbschema.RelationID;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Objects;

/**
 * Creates QuotedIdentifiers following the rules of SparkSQL:
 *    - double and single quotes are not tolerated for schema and attributes definition
 *    - you need to use backticks
 */
@IDFactoryType("SPARK")
@NonNullByDefault
public class SparkSQLQuotedIDFactory extends SQLStandardQuotedIDFactory {

    public static final String SQL_QUOTATION_STRING = "`";

    @Override
    public QuotedID createAttributeID(String s) {
        return createFromString(s);
    }

    @Override
    public RelationID createRelationID(String tableId) {
        return new RelationIDImpl(ImmutableList.of(createFromString(tableId)));
    }

    @Override
    protected QuotedID createFromString(String s) {
        Objects.requireNonNull(s);

        if (s.startsWith(SQL_QUOTATION_STRING) && s.endsWith(SQL_QUOTATION_STRING))
            return new QuotedIDImpl(s.substring(1, s.length() - 1), SQL_QUOTATION_STRING, false);

        return new QuotedIDImpl(s, SQL_QUOTATION_STRING, false);
    }

    @Override
    public String getIDQuotationString() {
        return SQL_QUOTATION_STRING;
    }

}
