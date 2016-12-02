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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

public class DBMetadata implements DataSourceMetadata {

	private static final long serialVersionUID = -806363154890865756L;
	private static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

	private final Map<RelationID, DatabaseRelationDefinition> tables;
	
	// relations include tables and views (views are only created for complex queries in mappings)
	private final Map<RelationID, RelationDefinition> relations;
	private final List<DatabaseRelationDefinition> listOfTables;

	private final String driverName;
	private final String driverVersion;
	private final String databaseProductName;
	private final String databaseVersion;
	private final QuotedIDFactory idfac;

	/**
	 * Constructs an initial metadata with some general information about the
	 * database, e.g., the driver name, the database engine name.
	 *
	 * DO NOT USE THIS CONSTRUCTOR -- USE MetadataExtractor METHODS INSTEAD
	 */

	DBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion, QuotedIDFactory idfac) {
		this(driverName, driverVersion, databaseProductName, databaseVersion, idfac, new HashMap<>(), new HashMap<>(),
				new LinkedList<>());
	}

	private DBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion,
					   QuotedIDFactory idfac, Map<RelationID, DatabaseRelationDefinition> tables,
					   Map<RelationID, RelationDefinition> relations, List<DatabaseRelationDefinition> listOfTables) {
		this.driverName = driverName;
		this.driverVersion = driverVersion;
		this.databaseProductName = databaseProductName;
		this.databaseVersion = databaseVersion;
		this.idfac = idfac;
		this.tables = tables;
		this.relations = relations;
		this.listOfTables = listOfTables;
	}

	/**
	 * creates a database table (which can also be a database view) 
	 * if the <name>id</name> contains schema than the relation is added 
	 * to the lookup table (see getDatabaseRelation and getRelation) with 
	 * both the fully qualified id and the table name only id
	 * 
	 * @param id
	 * @return
	 */
	
	public DatabaseRelationDefinition createDatabaseRelation(RelationID id) {
		DatabaseRelationDefinition table = new DatabaseRelationDefinition(id);
		add(table, tables);
		add(table, relations);
		listOfTables.add(table);
		return table;
	}

	
	private int parserViewCounter;
	
	/**
	 * creates a view for SQLQueryParser
	 * (NOTE: these views are simply names for complex non-parsable subqueries, not database views)
	 *
	 * @param sql
	 * @return
	 */
	
	public ParserViewDefinition createParserView(String sql) {
		RelationID id = idfac.createRelationID(null, String.format("view_%s", parserViewCounter++));	
		
		ParserViewDefinition view = new ParserViewDefinition(id, sql);
		add(view, relations);
		return view;
	}
	
	/**
	 * Inserts a new data definition to this metadata object. 
	 * 
	 * @param td
	 *            The data definition. It can be a {@link DatabaseRelationDefinition} or a
	 *            {@link ParserViewDefinition} object.
	 */
	private <T extends RelationDefinition> void add(T td, Map<RelationID, T> schema) {
		schema.put(td.getID(), td);
		if (td.getID().hasSchema()) {
			RelationID noSchemaID = td.getID().getSchemalessID();
			if (!schema.containsKey(noSchemaID)) {
				schema.put(noSchemaID, td);
			}
			else {
				System.err.println("DUPLICATE TABLE NAMES, USE QUALIFIED NAMES:\n" + td + "\nAND\n" + schema.get(noSchemaID));
				//schema.remove(noSchemaID);
				// TODO (ROMAN 8 Oct 2015): think of a better way of resolving ambiguities 
			}
		}
	}

	
	/**
	 * Retrieves the data definition object based on its name. The
	 * <name>id</name> is a table name.
	 * If <name>id</name> has schema and the fully qualified id 
	 * cannot be resolved the the table-only id is used  
	 * 
	 * @param id
	 */
	@Override
	public DatabaseRelationDefinition getDatabaseRelation(RelationID id) {
		DatabaseRelationDefinition def = tables.get(id);
		if (def == null && id.hasSchema()) {
			def = tables.get(id.getSchemalessID());
		}
		return def;
	}

	/**
	 * Retrieves the data definition object based on its name. The
	 * <name>name</name> can be either a table name or a view name.
	 * If <name>id</name> has schema and the fully qualified id 
	 * cannot be resolved the the table-only id is used  
	 * 
	 * @param name
	 */
	public RelationDefinition getRelation(RelationID name) {
		RelationDefinition def = relations.get(name);
		if (def == null && name.hasSchema()) {
			def = relations.get(name.getSchemalessID());
		}
		return def;
	}
	
	/**
	 * Retrieves the tables list form the metadata.
	 */
	@Override
	public Collection<DatabaseRelationDefinition> getDatabaseRelations() {
		return Collections.unmodifiableCollection(listOfTables);
	}


	public String getDriverName() {
		return driverName;
	}

	public String getDriverVersion() {
		return driverVersion;
	}

	@Override
	public String printKeys() {
		StringBuilder builder = new StringBuilder();
		Collection<DatabaseRelationDefinition> table_list = getDatabaseRelations();
		// Prints all primary keys
		builder.append("\n====== Unique constraints ==========\n");
		for (DatabaseRelationDefinition dd : table_list) {
			builder.append(dd + ";\n");
			for (UniqueConstraint uc : dd.getUniqueConstraints())
				builder.append(uc + ";\n");
			builder.append("\n");
		}
		// Prints all foreign keys
		builder.append("====== Foreign key constraints ==========\n");
		for(DatabaseRelationDefinition dd : table_list) {
			for (ForeignKeyConstraint fk : dd.getForeignKeys())
				builder.append(fk + ";\n");
		}
		return builder.toString();
	}

	/***
	 * Generates a map for each predicate in the body of the rules in 'program'
	 * that contains the Primary Key data for the predicates obtained from the
	 * info in the metadata.
	 *
	 * It also returns the columns with unique constraints
	 *
	 * For instance, Given the table definition
	 *   Tab0[col1:pk, col2:pk, col3, col4:unique, col5:unique],
	 *
	 * The methods will return the following Multimap:
	 *  { Tab0 -> { [col1, col2], [col4], [col5] } }
	 *
	 *
	 */
	@Override
	public ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> extractUniqueConstraints() {
		Map<Predicate, AtomPredicate> predicateCache = new HashMap<>();

		return getDatabaseRelations().stream()
				.flatMap(relation -> extractUniqueConstraintsFromRelation(relation, predicateCache))
				.collect(ImmutableCollectors.toMultimap());
	}

	private static Stream<Map.Entry<AtomPredicate, ImmutableList<Integer>>> extractUniqueConstraintsFromRelation(
			DatabaseRelationDefinition relation, Map<Predicate, AtomPredicate> predicateCache) {

		Predicate originalPredicate = Relation2DatalogPredicate.createPredicateFromRelation(relation);
		AtomPredicate atomPredicate = convertToAtomPredicate(originalPredicate, predicateCache);

		return relation.getUniqueConstraints().stream()
				.map(uc -> uc.getAttributes().stream()
						.map(Attribute::getIndex)
						.collect(ImmutableCollectors.toList()))
				.map(positions -> new AbstractMap.SimpleEntry<>(atomPredicate, positions));
	}

	/**
	 * generate CQIE rules from foreign key info of db metadata
	 * TABLE1.COL1 references TABLE2.COL2 as foreign key then
	 * construct CQIE rule TABLE2(P1, P3, COL2, P4) :- TABLE1(COL2, T2, T3).
	 */
	@Override
	public ImmutableMultimap<AtomPredicate, CQIE> generateFKRules() {
		final boolean printouts = false;

		if (printouts)
			System.out.println("===FOREIGN KEY RULES");
		int count = 0;

		ImmutableMultimap.Builder<AtomPredicate, CQIE> multimapBuilder = ImmutableMultimap.builder();
		Map<Predicate, AtomPredicate> knownPredicateMap = new HashMap<>();

		Collection<DatabaseRelationDefinition> tableDefs = getDatabaseRelations();
		for (DatabaseRelationDefinition def : tableDefs) {
			for (ForeignKeyConstraint fks : def.getForeignKeys()) {

				DatabaseRelationDefinition def2 = fks.getReferencedRelation();

				Map<Integer, Integer> positionMatch = new HashMap<>();
				for (ForeignKeyConstraint.Component comp : fks.getComponents()) {
					// Get current table and column (1)
					Attribute att1 = comp.getAttribute();

					// Get referenced table and column (2)
					Attribute att2 = comp.getReference();

					// Get positions of referenced attribute
					int pos1 = att1.getIndex();
					int pos2 = att2.getIndex();
					positionMatch.put(pos1 - 1, pos2 - 1); // indexes start at 1
				}
				// Construct CQIE
				int len1 = def.getAttributes().size();
				List<Term> terms1 = new ArrayList<>(len1);
				for (int i = 1; i <= len1; i++)
					terms1.add(DATA_FACTORY.getVariable("t" + i));

				// Roman: important correction because table2 may not be in the same case
				// (e.g., it may be all upper-case)
				int len2 = def2.getAttributes().size();
				List<Term> terms2 = new ArrayList<>(len2);
				for (int i = 1; i <= len2; i++)
					terms2.add(DATA_FACTORY.getVariable("p" + i));

				// do the swapping
				for (Map.Entry<Integer,Integer> swap : positionMatch.entrySet())
					terms1.set(swap.getKey(), terms2.get(swap.getValue()));

				Function head = Relation2DatalogPredicate.getAtom(def2, terms2);
				Function body = Relation2DatalogPredicate.getAtom(def, terms1);

				CQIE rule = DATA_FACTORY.getCQIE(head, body);
				multimapBuilder.put(convertToAtomPredicate(body.getFunctionSymbol(), knownPredicateMap), rule);
				if (printouts)
					System.out.println("   FK_" + ++count + " " +  head + " :- " + body);
			}
		}
		if (printouts)
			System.out.println("===END OF FOREIGN KEY RULES");
		return multimapBuilder.build();
	}

	private static AtomPredicate convertToAtomPredicate(Predicate originalPredicate,
														Map<Predicate, AtomPredicate> predicateCache) {
		if (originalPredicate instanceof AtomPredicate) {
			return (AtomPredicate) originalPredicate;
		}
		else if (predicateCache.containsKey(originalPredicate)) {
			return predicateCache.get(originalPredicate);
		}
		else {
			AtomPredicate atomPredicate = new AtomPredicateImpl(originalPredicate);
			// Cache it
			predicateCache.put(originalPredicate, atomPredicate);
			return atomPredicate;
		}
	}

	public String getDbmsProductName() {
		return databaseProductName;
	}
	
	public String getDbmsVersion() {
		return databaseVersion;
	}
	

	public QuotedIDFactory getQuotedIDFactory() {
		return idfac;
	}
	
	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		for (RelationID key : relations.keySet()) {
			bf.append(key);
			bf.append("=");
			bf.append(relations.get(key).toString());
			bf.append("\n");
		}
		return bf.toString();
	}

	@Override
	public DBMetadata clone() {
		return new DBMetadata(driverName, driverVersion, databaseProductName, databaseVersion, idfac,
				new HashMap<>(tables), new HashMap<>(relations), new LinkedList<>(listOfTables));
	}
}
