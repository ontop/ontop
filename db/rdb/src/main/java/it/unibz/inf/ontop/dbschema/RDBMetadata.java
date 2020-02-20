package it.unibz.inf.ontop.dbschema;

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
import it.unibz.inf.ontop.model.type.DBTypeFactory;


public class RDBMetadata extends BasicDBMetadata {

	private int parserViewCounter;

	protected final DBTypeFactory dbTypeFactory;

	/**
	 * Constructs an initial metadata with some general information about the
	 * database, e.g., the driver name, the database engine name.
	 *
	 * DO NOT USE THIS CONSTRUCTOR -- USE MetadataExtractor METHODS INSTEAD
	 */

	RDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion,
				QuotedIDFactory idfac, DBTypeFactory dbTypeFactory) {
		super(driverName, driverVersion, databaseProductName, databaseVersion, idfac);
		this.dbTypeFactory = dbTypeFactory;
	}

	/**
	 * creates a view for SQLQueryParser
	 * (NOTE: these views are simply names for complex non-parsable subqueries, not database views)
	 *
	 * TODO: make the second argument a callback (which is called only when needed)
     * TODO: make it re-use parser views for the same SQL
	 *
	 * @param sql
	 * @return
	 */

	public ParserViewDefinition createParserView(String sql, ImmutableList<QuotedID> attributes) {
		if (!isStillMutable()) {
			throw new IllegalStateException("Too late! Parser views must be created before freezing the DBMetadata");
		}
		RelationID id = getQuotedIDFactory().createRelationID(null, String.format("view_%s", parserViewCounter++));

		return new ParserViewDefinition(id, attributes, sql, dbTypeFactory);
	}
}
