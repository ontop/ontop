package it.unibz.inf.ontop.sql;

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


import java.util.*;

public class RDBMetadata extends BasicDBMetadata {

	private static final long serialVersionUID = -806363154890865756L;
	private int parserViewCounter;

	/**
	 * Constructs an initial metadata with some general information about the
	 * database, e.g., the driver name, the database engine name.
	 *
	 * DO NOT USE THIS CONSTRUCTOR -- USE MetadataExtractor METHODS INSTEAD
	 */

	RDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion, QuotedIDFactory idfac) {
		super(driverName, driverVersion, databaseProductName, databaseVersion, idfac);
	}

	private RDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion,
							  QuotedIDFactory idfac, Map<RelationID, DatabaseRelationDefinition> tables,
							  Map<RelationID, RelationDefinition> relations, List<DatabaseRelationDefinition> listOfTables,
							  int parserViewCounter) {
		super(driverName, driverVersion, databaseProductName, databaseVersion, idfac, tables, relations, listOfTables);
		this.parserViewCounter = parserViewCounter;
	}
	
	/**
	 * creates a view for SQLQueryParser
	 * (NOTE: these views are simply names for complex non-parsable subqueries, not database views)
	 *
	 * @param sql
	 * @return
	 */
	
	public ParserViewDefinition createParserView(String sql) {
		if (!isStillMutable()) {
			throw new IllegalStateException("Too late! Parser views must be created before freezing the DBMetadata");
		}
		RelationID id = getQuotedIDFactory().createRelationID(null, String.format("view_%s", parserViewCounter++));
		
		ParserViewDefinition view = new ParserViewDefinition(id, sql);
		// UGLY!!
		add(view, relations);
		return view;
	}

	@Deprecated
	@Override
	public RDBMetadata clone() {
		return new RDBMetadata(getDriverName(), getDriverVersion(), getDbmsProductName(), getDbmsVersion(), getQuotedIDFactory(),
				new HashMap<>(getTables()), new HashMap<>(relations), new LinkedList<>(getDatabaseRelations()), parserViewCounter);
	}
}
