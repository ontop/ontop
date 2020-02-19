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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.*;

public class RDBMetadata extends BasicDBMetadata {

	private int parserViewCounter;

	private final TypeFactory typeFactory;

	/**
	 * Constructs an initial metadata with some general information about the
	 * database, e.g., the driver name, the database engine name.
	 *
	 * DO NOT USE THIS CONSTRUCTOR -- USE MetadataExtractor METHODS INSTEAD
	 */

	RDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion,
				QuotedIDFactory idfac, TypeFactory typeFactory) {
		super(driverName, driverVersion, databaseProductName, databaseVersion, idfac);
		this.typeFactory = typeFactory;
	}


	private RDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion,
						QuotedIDFactory idfac, Map<RelationID, DatabaseRelationDefinition> tables,
						Map<RelationID, RelationDefinition> relations, List<DatabaseRelationDefinition> listOfTables,
						int parserViewCounter, TypeFactory typeFactory) {
		super(driverName, driverVersion, databaseProductName, databaseVersion, tables, relations,
				listOfTables, idfac);
		this.parserViewCounter = parserViewCounter;
		this.typeFactory = typeFactory;
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

		ParserViewDefinition view = new ParserViewDefinition(id, attributes, sql, typeFactory.getDBTypeFactory());
		// UGLY!!
		add(view, relations);
		return view;
	}

	@Deprecated
	@Override
	public RDBMetadata clone() {
		throw new RuntimeException("METADATA CLONE");
//		return new RDBMetadata(getDriverName(), getDriverVersion(), getDbmsProductName(), getDbmsVersion(), getQuotedIDFactory(),
//				new HashMap<>(getTables()), new HashMap<>(relations), new LinkedList<>(getDatabaseRelations()),
//				parserViewCounter, typeFactory);
	}

	@Deprecated
	public RDBMetadata copyOf() {
		return new RDBMetadata(getDriverName(), getDriverVersion(), getDbmsProductName(), getDbmsVersion(), getQuotedIDFactory(),
				new HashMap<>(getTables()), new HashMap<>(relations), new LinkedList<>(getDatabaseRelations()),
				parserViewCounter, typeFactory);
	}

	@JsonIgnore
    public DBTypeFactory getDBTypeFactory() {
		return typeFactory.getDBTypeFactory();
    }

    @JsonProperty("metadata")
	Map<String, String> getMedadataForJsonExport(){
		return ImmutableMap.of(
				"dbmsProductName", this.getDbmsProductName(),
				"dbmsVersion", this.getDbmsVersion(),
				"driverName", this.getDriverName(),
				"driverVersion", this.getDriverVersion(),
				"quotationString", this.getDBParameters().getQuotedIDFactory().getIDQuotationString()
		);
	}
}
