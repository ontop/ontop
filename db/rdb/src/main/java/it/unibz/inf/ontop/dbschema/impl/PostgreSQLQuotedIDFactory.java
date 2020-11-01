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

import static it.unibz.inf.ontop.dbschema.impl.SQLStandardQuotedIDFactory.QUOTATION_STRING;

/**
 * Creates QuotedIdentifiers following the rules of PostrgeSQL:<br>
 *    - unquoted identifiers are converted into lower case<br>
 *    - quoted identifiers are preserved
 * 
 * PostgreSQL
 * ----------
 *
 * http://www.postgresql.org/docs/9.1/static/sql-syntax-lexical.html
 *
 * Unquoted names are always folded to lower (!) case.
 *
 * Quoted identifier is formed by enclosing an arbitrary sequence of characters in double-quotes (").
 * (To include a double quote, write two double quotes.)
 *
 * A variant of quoted identifiers allows including escaped Unicode characters identified by their code points.
 * This variant starts with U& (upper or lower case U followed by ampersand) immediately before the opening
 * double quote, without any spaces in between, for example U&"foo".
 *
 * @author Roman Kontchakov
 *
 */

public class PostgreSQLQuotedIDFactory extends SQLStandardQuotedIDFactory {

	@Override
	protected QuotedID createFromString(@Nonnull String s) {
		Objects.requireNonNull(s);

		if (s.startsWith(QUOTATION_STRING) && s.endsWith(QUOTATION_STRING))
			return new QuotedIDImpl(s.substring(1, s.length() - 1), QUOTATION_STRING);

		return new QuotedIDImpl(s.toLowerCase(), NO_QUOTATION);
	}
}
