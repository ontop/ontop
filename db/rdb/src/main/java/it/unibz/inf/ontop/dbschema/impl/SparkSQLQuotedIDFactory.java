package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory.IDFactoryType;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Creates QuotedIdentifiers following the rules of SparkSQL:
 *    - double and single quotes are not tolerated for schema and attributes definition
 *    - you need to use backticks
 */
@IDFactoryType("SPARK")
@NonNullByDefault
public class SparkSQLQuotedIDFactory extends AbstractBacktickQuotedIDFactory {

    @Override
    protected QuotedID createFromString(String s) {
        return createFromString(s, BACKTICK_QUOTATION_STRING, i -> i, BACKTICK_QUOTATION_STRING, false);
    }
}
