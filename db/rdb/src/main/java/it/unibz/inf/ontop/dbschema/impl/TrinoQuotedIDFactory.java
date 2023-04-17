package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory.IDFactoryType;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Objects;

@IDFactoryType("TRINO")
@NonNullByDefault
public class TrinoQuotedIDFactory extends SQLStandardQuotedIDFactory {

	@Override
	protected QuotedID createFromString(String s) {
		Objects.requireNonNull(s);

		if (s.startsWith(QUOTATION_STRING) && s.endsWith(QUOTATION_STRING))
			return new QuotedIDImpl(s.substring(1, s.length() - 1), QUOTATION_STRING);

		return new QuotedIDImpl(s, QUOTATION_STRING);
	}

}
