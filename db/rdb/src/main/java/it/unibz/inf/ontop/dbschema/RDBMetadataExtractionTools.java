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
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * Retrieves the database metadata (table schema and database constraints)
 *
 * @author Roman Kontchakov
 *
 */

/**
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
 * MS SQL Server
 * -------------
 *
 * https://msdn.microsoft.com/en-us/library/ms378535(v=sql.110).aspx
 *
 * When using the Microsoft JDBC Driver with a SQL Server database,
 * getIdentifierQuoteString returns double quotation marks ("").
 *
 *
 * MySQL
 * -----
 *
 * http://dev.mysql.com/doc/refman/5.0/en/identifier-case-sensitivity.html
 *
 * How table and database names are stored on disk and used in MySQL is affected
 * by the lower_case_table_names system variable, which you can set when starting mysqld.
 *
 * Column, index, and stored routine names are not case sensitive on any platform, nor are column aliases.
 *
 */

public class RDBMetadataExtractionTools {

	private static final boolean printouts = false;

	private static Logger log = LoggerFactory.getLogger(RDBMetadataExtractionTools.class);

	/**
	 * Creates database metadata description (but does not load metadata)
	 *
	 * @return The database metadata object.
	 * @throws SQLException
	 */

	public static BasicDBMetadata createMetadata(Connection conn,
											 DBTypeFactory dbTypeFactory) throws SQLException  {

		final DatabaseMetaData md = conn.getMetaData();
		String productName = md.getDatabaseProductName();
		if (printouts) {
			System.out.println("=================================\nDBMetadataExtractor REPORT: " + productName);
			System.out.println("storesLowerCaseIdentifiers: " + md.storesLowerCaseIdentifiers());
			System.out.println("storesUpperCaseIdentifiers: " + md.storesUpperCaseIdentifiers());
			System.out.println("storesMixedCaseIdentifiers: " + md.storesMixedCaseIdentifiers());
			System.out.println("supportsMixedCaseIdentifiers: " + md.supportsMixedCaseIdentifiers());
			System.out.println("storesLowerCaseQuotedIdentifiers: " + md.storesLowerCaseQuotedIdentifiers());
			System.out.println("storesUpperCaseQuotedIdentifiers: " + md.storesUpperCaseQuotedIdentifiers());
			System.out.println("storesMixedCaseQuotedIdentifiers: " + md.storesMixedCaseQuotedIdentifiers());
			System.out.println("supportsMixedCaseQuotedIdentifiers: " + md.supportsMixedCaseQuotedIdentifiers());
			System.out.println("getIdentifierQuoteString: " + md.getIdentifierQuoteString());
		}

		QuotedIDFactory idfac;
		//  MySQL
		if (productName.contains("MySQL"))  {
			//System.out.println("getIdentifierQuoteString: " + md.getIdentifierQuoteString());
			idfac = new QuotedIDFactoryMySQL(md.storesMixedCaseIdentifiers(), "`");
		}
		else if (md.storesMixedCaseIdentifiers()) {
			// treat Exareme as a case-sensitive DB engine (like MS SQL Server)
			// "SQL Server" = MS SQL Server
			idfac = new QuotedIDFactoryIdentity("\"");
		}
		else {
			if (md.storesLowerCaseIdentifiers())
				// PostgreSQL treats unquoted identifiers as lower-case
				idfac = new QuotedIDFactoryLowerCase("\"");
			else if (md.storesUpperCaseIdentifiers())
				// Oracle, DB2, H2, HSQL
				idfac = new QuotedIDFactoryStandardSQL("\"");
			else {
				log.warn("Unknown combination of identifier handling rules: " + md.getDatabaseProductName());
				log.warn("storesLowerCaseIdentifiers: " + md.storesLowerCaseIdentifiers());
				log.warn("storesUpperCaseIdentifiers: " + md.storesUpperCaseIdentifiers());
				log.warn("storesMixedCaseIdentifiers: " + md.storesMixedCaseIdentifiers());
				log.warn("supportsMixedCaseIdentifiers: " + md.supportsMixedCaseIdentifiers());
				log.warn("storesLowerCaseQuotedIdentifiers: " + md.storesLowerCaseQuotedIdentifiers());
				log.warn("storesUpperCaseQuotedIdentifiers: " + md.storesUpperCaseQuotedIdentifiers());
				log.warn("storesMixedCaseQuotedIdentifiers: " + md.storesMixedCaseQuotedIdentifiers());
				log.warn("supportsMixedCaseQuotedIdentifiers: " + md.supportsMixedCaseQuotedIdentifiers());
				log.warn("getIdentifierQuoteString: " + md.getIdentifierQuoteString());

				idfac = new QuotedIDFactoryStandardSQL("\"");
			}
		}

		BasicDBMetadata metadata = new BasicDBMetadata(md.getDriverName(), md.getDriverVersion(),
							productName, md.getDatabaseProductVersion(), idfac, dbTypeFactory);
		
		return metadata;	
	}

	/**
	 * Retrieves the database metadata (table schema and database constraints)
	 *
	 * This method either uses the given list of tables or
	 *    if it is null then it retrieves all the complete list of tables from
	 *    the connection metadata
	 *
	 * @return The database metadata object.
	 */

	public static void loadMetadata(BasicDBMetadata metadata, DBTypeFactory dbTypeFactory, Connection conn, ImmutableList<RelationID> realTables) throws SQLException {

		if (printouts)
			System.out.println("GETTING METADATA WITH " + conn + " ON " + realTables);

		final DatabaseMetaData md = conn.getMetaData();
		String productName = md.getDatabaseProductName();

		QuotedIDFactory idfac =  metadata.getDBParameters().getQuotedIDFactory();

		RDBMetadataLoader metadataLoader;
		if (productName.contains("Oracle"))
			metadataLoader = new OracleJDBCRDBMetadataLoader(conn, idfac);
		else if (productName.contains("DB2"))
			metadataLoader = new DB2RDBMetadataLoader(conn, idfac);
		else if (productName.contains("SQL Server"))
			metadataLoader = new MSSQLDBMetadataLoader(conn, idfac);
		else
			metadataLoader = new JDBCRDBMetadataLoader(conn, idfac);

		List<RelationID> seedRelationIds;
		if (realTables == null || realTables.isEmpty())
			seedRelationIds = metadataLoader.getRelationIDs();
		else
			seedRelationIds = metadataLoader.getRelationIDs(realTables);

		List<RelationDefinition.AttributeListBuilder> extractedRelations = new LinkedList<>();

        String catalog = getCatalog(metadata, conn);

        for (RelationID seedId : seedRelationIds) {
			// the same seedId can be mapped to many tables (if the seedId has no schema)
			// we collect attributes from all of them
			RelationDefinition.AttributeListBuilder currentRelation = null;

			// catalog is ignored for now (rs.getString("TABLE_CAT"))
            try (ResultSet rs = md.getColumns(catalog, seedId.getSchemaName(), seedId.getTableName(), null)) {
				while (rs.next()) {
					String schema = rs.getString("TABLE_SCHEM");
					// MySQL workaround
					if (schema == null)
						schema = rs.getString("TABLE_CAT");

					RelationID relationId = RelationID.createRelationIdFromDatabaseRecord(idfac, schema,
										rs.getString("TABLE_NAME"));
					QuotedID attributeId = QuotedID.createIdFromDatabaseRecord(idfac, rs.getString("COLUMN_NAME"));
					if (printouts)
						System.out.println("         " + relationId + "." + attributeId);

					if (currentRelation == null || !currentRelation.getRelationID().equals(relationId)) {
						// switch to the next database relation
						currentRelation = new RelationDefinition.AttributeListBuilder(relationId);
						extractedRelations.add(currentRelation);
					}

					// columnNoNulls, columnNullable, columnNullableUnknown
					boolean isNullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
					String typeName = rs.getString("TYPE_NAME");
					int columnSize = rs.getInt("COLUMN_SIZE");
					DBTermType termType = dbTypeFactory.getDBTermType(typeName, columnSize);

					currentRelation.addAttribute(attributeId, termType, typeName, isNullable);
				}
			}
		}

        List<DatabaseRelationDefinition> extractedRelations2 = new ArrayList<>();
		for (RelationDefinition.AttributeListBuilder r : extractedRelations) {
			DatabaseRelationDefinition relation = metadata.createDatabaseRelation(r);
			extractedRelations2.add(relation);
		}

		for (DatabaseRelationDefinition relation : extractedRelations2)	{
			getPrimaryKey(md, relation, metadata.getDBParameters().getQuotedIDFactory());
			getUniqueAttributes(md, relation, metadata.getDBParameters().getQuotedIDFactory());
			getForeignKeys(md, relation, metadata);
			if (printouts) {
				System.out.println(relation + ";");
				for (UniqueConstraint uc : relation.getUniqueConstraints())
					System.out.println(uc + ";");
				for (ForeignKeyConstraint fk : relation.getForeignKeys())
					System.out.println(fk +  ";");
				System.out.println("");
			}
		}

		if (printouts) {
			System.out.println("RESULTING METADATA:\n" + metadata);
			System.out.println("DBMetadataExtractor END OF REPORT\n=================================");
		}
	}

    private static String getCatalog(BasicDBMetadata metadata, Connection conn) throws SQLException {
        String catalog = null;
        if (metadata.getDBParameters().getDbmsProductName().contains("MySQL")) {
            try (Statement statement = conn.createStatement();
				 ResultSet rs = statement.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) {
                    catalog = rs.getString(1);
                }
            }
        }
        return catalog;
    }






	/**
	 * Retrieves the primary key for the table
	 *
	 */
	private static void getPrimaryKey(DatabaseMetaData md, DatabaseRelationDefinition relation, QuotedIDFactory idfac) throws SQLException {
		RelationID id = relation.getID();
		// Retrieves a description of the given table's primary key columns. They are ordered by COLUMN_NAME (sic!)
		try (ResultSet rs = md.getPrimaryKeys(null, id.getSchemaName(), id.getTableName())) {
			extractPrimaryKey(relation, idfac, id, rs);
		}
		catch (SQLSyntaxErrorException e) {
		    // WORKAROUND for MySQL connector >= 8.0:
            // <https://github.com/ontop/ontop/issues/270>
            try (ResultSet rs = md.getPrimaryKeys(id.getSchemaName(), null, id.getTableName())) {
                extractPrimaryKey(relation, idfac, id, rs);
            }
		}
	}

	private static void extractPrimaryKey(DatabaseRelationDefinition relation, QuotedIDFactory idfac, RelationID id, ResultSet rs) throws SQLException {
		Map<Integer, String> primaryKeyAttributes = new HashMap<>();
		String currentName = null;
		while (rs.next()) {
			// TABLE_CAT is ignored for now; assume here that relation has a fully specified name
			RelationID id2 = RelationID.createRelationIdFromDatabaseRecord(idfac,
								rs.getString("TABLE_SCHEM"), rs.getString("TABLE_NAME"));
			if (id2.equals(id)) {
				currentName = rs.getString("PK_NAME"); // may be null
				String attr = rs.getString("COLUMN_NAME");
				int seq = rs.getShort("KEY_SEQ");
				primaryKeyAttributes.put(seq, attr);
			}
		}
		if (!primaryKeyAttributes.isEmpty()) {
			// use the KEY_SEQ values to restore the correct order of attributes in the PK
			UniqueConstraint.BuilderImpl builder = UniqueConstraint.primaryKeyBuilder(relation, currentName);
			for (int i = 1; i <= primaryKeyAttributes.size(); i++) {
				QuotedID attrId = QuotedID.createIdFromDatabaseRecord(idfac, primaryKeyAttributes.get(i));
				builder.addDeterminant(relation.getAttribute(attrId));
			}
			relation.addUniqueConstraint(builder.build());
		}
	}

	/**
	 * Retrieves the unique attributes(s)
	 * @param md
	 * @return
	 * @throws SQLException
	 */
	private static void getUniqueAttributes(DatabaseMetaData md, DatabaseRelationDefinition relation, QuotedIDFactory idfac) throws SQLException {

		RelationID id = relation.getID();
		// extracting unique
		try (ResultSet rs = md.getIndexInfo(null, id.getSchemaName(), id.getTableName(), true, true)) {
            extractUniqueAttributes(relation, idfac, rs);
        }
		catch (Exception e){
		    // Workaround for MySQL-connector >= 8.0
            try (ResultSet rs = md.getIndexInfo(id.getSchemaName(),null, id.getTableName(), true, true)) {
                extractUniqueAttributes(relation, idfac, rs);
            }
        }
	}

    private static void extractUniqueAttributes(DatabaseRelationDefinition relation, QuotedIDFactory idfac, ResultSet rs) throws SQLException {
        UniqueConstraint.BuilderImpl builder = null;
        while (rs.next()) {
            // TYPE: tableIndexStatistic - this identifies table statistics that are returned in conjunction with a table's index descriptions
            //       tableIndexClustered - this is a clustered index
            //       tableIndexHashed - this is a hashed index
            //       tableIndexOther (all are static final int in DatabaseMetaData)
            if (rs.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                if (builder != null)
                    relation.addUniqueConstraint(builder.build());

                builder = null;
                continue;
            }
            if (rs.getShort("ORDINAL_POSITION") == 1) {
                if (builder != null)
                    relation.addUniqueConstraint(builder.build());

                // TABLE_CAT is ignored for now; assume here that relation has a fully specified name
                // and so, no need to check whether TABLE_SCHEM and TABLE_NAME match

                if (!rs.getBoolean("NON_UNIQUE")) {
					String name = rs.getString("INDEX_NAME");
                    builder = UniqueConstraint.builder(relation, name);
                }
                else
                    builder = null;
            }

            if (builder != null) {
                QuotedID attrId = QuotedID.createIdFromDatabaseRecord(idfac, rs.getString("COLUMN_NAME"));
                // ASC_OR_DESC String => column sort sequence, "A" => ascending, "D" => descending,
                //        may be null if sort sequence is not supported; null when TYPE is tableIndexStatistic
                // CARDINALITY int => When TYPE is tableIndexStatistic, then this is the number of rows in the table;
                //                      otherwise, it is the number of unique values in the index.
                // PAGES int => When TYPE is tableIndexStatisic then this is the number of pages used for the table,
                //                    otherwise it is the number of pages used for the current index.
                // FILTER_CONDITION String => Filter condition, if any. (may be null)
                Attribute attr = relation.getAttribute(attrId);
                if (attr == null) { // Compensate for the bug in PostgreSQL JBDC driver that
                    // strips off the quotation marks
                    attrId = QuotedID.createIdFromDatabaseRecord(idfac, "\"" + rs.getString("COLUMN_NAME") + "\"");
                    attr = relation.getAttribute(attrId);
                }
                builder.addDeterminant(attr);
            }
        }
        if (builder != null)
            relation.addUniqueConstraint(builder.build());
    }

    /**
	 * Retrieves the foreign keys for the table
	 *
	 */
	private static void getForeignKeys(DatabaseMetaData md, DatabaseRelationDefinition relation, DBMetadata metadata) throws SQLException {

		QuotedIDFactory idfac = metadata.getDBParameters().getQuotedIDFactory();

		RelationID relationId = relation.getID();
		try (ResultSet rs = md.getImportedKeys(null, relationId.getSchemaName(), relationId.getTableName())) {
            extractForeignKeys(relation, metadata, idfac, rs);
        }
        catch (Exception ex) {
            try (ResultSet rs = md.getImportedKeys(relationId.getSchemaName(),null, relationId.getTableName())) {
                extractForeignKeys(relation, metadata, idfac, rs);
            }
        }
	}

    private static void extractForeignKeys(DatabaseRelationDefinition relation, DBMetadata metadata, QuotedIDFactory idfac, ResultSet rs) throws SQLException {
        ForeignKeyConstraint.Builder builder = null;
        String currentName = null;
        while (rs.next()) {
			String schemaName = rs.getString("PKTABLE_SCHEM");

			// WORKAROUND FOR MySQL connector >= v8.0
			if (schemaName == null) {
			    schemaName = rs.getString("PKTABLE_CAT");
            }

			RelationID refId = RelationID.createRelationIdFromDatabaseRecord(idfac,
					schemaName, rs.getString("PKTABLE_NAME"));
            DatabaseRelationDefinition ref = metadata.getDatabaseRelation(refId);
            // FKTABLE_SCHEM and FKTABLE_NAME are ignored for now
            int seq = rs.getShort("KEY_SEQ");
            if (seq == 1) {
                if (builder != null)
                    relation.addForeignKeyConstraint(builder.build(currentName));

                currentName = rs.getString("FK_NAME"); // String => foreign key name (may be null)

                if (ref != null) {
                    builder = new ForeignKeyConstraint.Builder(relation, ref);
                }
                else {
                    builder = null; // do not add this foreign key
                                    // because there is no table it refers to
                    log.warn("Cannot find table: " + refId + " for FK " + currentName);
                }
            }
            if (builder != null) {
                QuotedID attrId = QuotedID.createIdFromDatabaseRecord(idfac, rs.getString("FKCOLUMN_NAME"));
                QuotedID refAttrId = QuotedID.createIdFromDatabaseRecord(idfac, rs.getString("PKCOLUMN_NAME"));
                builder.add(relation.getAttribute(attrId), ref.getAttribute(refAttrId));
            }
        }
        if (builder != null)
            relation.addForeignKeyConstraint(builder.build(currentName));
    }
}
