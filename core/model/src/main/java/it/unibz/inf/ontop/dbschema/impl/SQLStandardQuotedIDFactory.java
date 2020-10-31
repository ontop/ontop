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


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Creates QuotedIdentifiers following the rules of SQL standard:<br>
 *    - unquoted identifiers are converted into upper case<br>
 *    - quoted identifiers are preserved
 *
 * H2
 * --
 *
 * http://h2database.com/html/grammar.html
 *
 * Names are not case sensitive (but it appears that the upper-case is the canonical form).
 *
 * Quoted names are case sensitive, and can contain spaces.
 * Two double quotes can be used to create a single double quote inside an identifier.
 *
 *
 * HSQLDB
 *
 * http://www.hsqldb.org/doc/1.8/src/org/hsqldb/jdbc/jdbcDatabaseMetaData.html
 *
 * HSQLDB treats unquoted identifiers as case insensitive in SQL but stores them in upper case;
 * it treats quoted identifiers as case sensitive and stores them verbatim. All jdbcDatabaseMetaData
 * methods perform case-sensitive comparison between name (pattern) arguments and the corresponding
 * identifier values as they are stored in the database.
 *
 * HSQLDB uses the standard SQL identifier quote character (the double quote character);
 * getIdentifierQuoteString() always returns ".
 *
 *
 * @author Roman Kontchakov
 *
 */

public class SQLStandardQuotedIDFactory implements QuotedIDFactory {

	public static final String QUOTATION_STRING = "\"";
	public static final String NO_QUOTATION = "";

	@Override
	public QuotedID createAttributeID(@Nonnull String s) {
		Objects.requireNonNull(s);
		return createFromString(s);
	}

	@Override
	public RelationID createRelationID(@Nonnull String tableId) {
		return new RelationIDImpl(ImmutableList.of(createFromString(tableId)));
	}


	@Override
	public RelationID createRelationID(String... components) {
		Objects.requireNonNull(components[components.length - 1]);
		ImmutableList.Builder<QuotedID> builder = ImmutableList.builder();
		for (int i = components.length - 1; i >= 0; i--)
			if (components[i] != null)
				builder.add(createFromString(components[i]));

		return new RelationIDImpl(builder.build());
	}
	
	protected QuotedID createFromString(@Nonnull String s) {
		Objects.requireNonNull(s);

		if (s.startsWith(QUOTATION_STRING) && s.endsWith(QUOTATION_STRING))
			return new QuotedIDImpl(s.substring(1, s.length() - 1), QUOTATION_STRING);

		return new QuotedIDImpl(s.toUpperCase(), NO_QUOTATION);
	}
	
	@Override
	public String getIDQuotationString() {
		return QUOTATION_STRING;
	}
	
}
