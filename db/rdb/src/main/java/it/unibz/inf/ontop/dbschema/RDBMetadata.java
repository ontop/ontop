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
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.*;

public class RDBMetadata extends BasicDBMetadata {

	private static final long serialVersionUID = -806363154890865756L;
	private int parserViewCounter;
	private final JdbcTypeMapper jdbcTypeMapper;

	/**
	 * Constructs an initial metadata with some general information about the
	 * database, e.g., the driver name, the database engine name.
	 *
	 * DO NOT USE THIS CONSTRUCTOR -- USE MetadataExtractor METHODS INSTEAD
	 */

	RDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion, QuotedIDFactory idfac,
				JdbcTypeMapper jdbcTypeMapper, AtomFactory atomFactory, Relation2Predicate relation2Predicate,
				TermFactory termFactory, DatalogFactory datalogFactory) {
		super(driverName, driverVersion, databaseProductName, databaseVersion, idfac, atomFactory, relation2Predicate,
				termFactory, datalogFactory);
		this.jdbcTypeMapper = jdbcTypeMapper;
	}

	@Override
	public Optional<TermType> getTermType(Attribute attribute) {
		return Optional.of(jdbcTypeMapper.getTermType(attribute.getType()));
	}

	private RDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion,
						QuotedIDFactory idfac, Map<RelationID, DatabaseRelationDefinition> tables,
						Map<RelationID, RelationDefinition> relations, List<DatabaseRelationDefinition> listOfTables,
						int parserViewCounter, JdbcTypeMapper jdbcTypeMapper, AtomFactory atomFactory,
						Relation2Predicate relation2Predicate, TermFactory termFactory, DatalogFactory datalogFactory) {
		super(driverName, driverVersion, databaseProductName, databaseVersion, idfac, tables, relations, listOfTables,
				relation2Predicate, atomFactory, termFactory, datalogFactory);
		this.parserViewCounter = parserViewCounter;
		this.jdbcTypeMapper = jdbcTypeMapper;
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
		
		ParserViewDefinition view = new ParserViewDefinition(id, attributes, sql);
		// UGLY!!
		add(view, relations);
		return view;
	}

	@Deprecated
	@Override
	public RDBMetadata clone() {
		return new RDBMetadata(getDriverName(), getDriverVersion(), getDbmsProductName(), getDbmsVersion(), getQuotedIDFactory(),
				new HashMap<>(getTables()), new HashMap<>(relations), new LinkedList<>(getDatabaseRelations()),
				parserViewCounter, jdbcTypeMapper, getAtomFactory(), getRelation2Predicate(), getTermFactory(),
				getDatalogFactory());
	}
}
