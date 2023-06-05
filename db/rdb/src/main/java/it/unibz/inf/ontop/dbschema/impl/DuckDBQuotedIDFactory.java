package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory.IDFactoryType;
import org.eclipse.jdt.annotation.NonNullByDefault;

@IDFactoryType("DUCKDB")
@NonNullByDefault
public class DuckDBQuotedIDFactory extends SQLStandardQuotedIDFactory {

	@Override
	protected QuotedID createFromString(String s) {
		return createFromString(s, QUOTATION_STRING, String::toLowerCase, QUOTATION_STRING, false);
	}
}
