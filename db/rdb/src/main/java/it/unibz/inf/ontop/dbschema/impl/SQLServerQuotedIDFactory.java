package it.unibz.inf.ontop.dbschema.impl;


/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import it.unibz.inf.ontop.dbschema.QuotedID;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Creates QuotedIdentifiers following the rules of MS SQL Server:<br>
 *    - unquoted identifiers are preserved<br>
 *    - quoted identifiers are preserved
 *
 *  MS SQL Server
 *  -------------
 *
 *  https://docs.microsoft.com/en-us/sql/connect/jdbc/reference/getidentifierquotestring-method-sqlserverdatabasemetadata?redirectedfrom=MSDN&view=sql-server-ver15
 *
 *  When using the Microsoft JDBC Driver with a SQL Server database,
 *  getIdentifierQuoteString returns double quotation marks ("").
 *
 * https://docs.microsoft.com/en-us/sql/relational-databases/databases/database-identifiers?view=sql-server-ver15
 *
 * @author Roman Kontchakov
 *
 */

public class SQLServerQuotedIDFactory extends SQLStandardQuotedIDFactory {

	@Override
	protected QuotedID createFromString(@Nonnull String s) {
		Objects.requireNonNull(s);

		if (s.startsWith(QUOTATION_STRING) && s.endsWith(QUOTATION_STRING))
			return new QuotedIDImpl(s.substring(1, s.length() - 1), QUOTATION_STRING);

		if (s.startsWith("[") && s.endsWith("]"))
			return new QuotedIDImpl(s.substring(1, s.length() - 1), QUOTATION_STRING);

		return new QuotedIDImpl(s, NO_QUOTATION);
	}
}
