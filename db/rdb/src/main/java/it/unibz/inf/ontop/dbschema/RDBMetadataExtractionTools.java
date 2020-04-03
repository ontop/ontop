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
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
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
 */

public class RDBMetadataExtractionTools {

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

		{
			log.info("=================================\nDBMetadataExtractor REPORT: {}", productName);
			log.info("storesLowerCaseIdentifiers: {}", md.storesLowerCaseIdentifiers());
			log.info("storesUpperCaseIdentifiers: {}", md.storesUpperCaseIdentifiers());
			log.info("storesMixedCaseIdentifiers: {}", md.storesMixedCaseIdentifiers());
			log.info("supportsMixedCaseIdentifiers: {}", md.supportsMixedCaseIdentifiers());
			log.info("storesLowerCaseQuotedIdentifiers: {}", md.storesLowerCaseQuotedIdentifiers());
			log.info("storesUpperCaseQuotedIdentifiers: {}", md.storesUpperCaseQuotedIdentifiers());
			log.info("storesMixedCaseQuotedIdentifiers: {}", md.storesMixedCaseQuotedIdentifiers());
			log.info("supportsMixedCaseQuotedIdentifiers: {}", md.supportsMixedCaseQuotedIdentifiers());
			log.info("getIdentifierQuoteString: {}", md.getIdentifierQuoteString());
		}

		QuotedIDFactory idfac;
		//  MySQL
		if (productName.contains("MySQL"))  {
			idfac = new QuotedIDFactoryMySQL(md.storesMixedCaseIdentifiers());
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
				idfac = new QuotedIDFactoryStandardSQL();
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

				idfac = new QuotedIDFactoryStandardSQL();
			}
		}

		BasicDBMetadata metadata = new BasicDBMetadata(md.getDriverName(), md.getDriverVersion(),
							productName, md.getDatabaseProductVersion(), idfac, dbTypeFactory);
		
		return metadata;	
	}

	public static void loadFullMetadata0(BasicDBMetadata metadata, Connection conn) throws SQLException, MetadataExtractionException {
		RDBMetadataExtractionTools.loadMetadata(metadata, conn, null);
	}

	public static BasicDBMetadata loadFullMetadata(Connection conn, DBTypeFactory dbTypeFactory) throws SQLException, MetadataExtractionException {
		BasicDBMetadata metadata = createMetadata(conn, dbTypeFactory);
		RDBMetadataExtractionTools.loadMetadata(metadata, conn, null);
		return metadata;
	}

	public static Collection<DatabaseRelationDefinition> loadAllRelations(Connection conn, DBTypeFactory dbTypeFactory) throws SQLException, MetadataExtractionException {
		BasicDBMetadata metadata = createMetadata(conn, dbTypeFactory);
		RDBMetadataExtractionTools.loadMetadata(metadata, conn, null);
		return metadata.getDatabaseRelations();
	}

	public static void loadMetadataForRelations(BasicDBMetadata metadata, Connection conn, ImmutableList<RelationID> realTables) throws SQLException, MetadataExtractionException {
		loadMetadata(metadata, conn, realTables);
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

	private static void loadMetadata(BasicDBMetadata metadata, Connection conn, ImmutableList<RelationID> realTables) throws MetadataExtractionException {

		DBTypeFactory dbTypeFactory = metadata.getDBParameters().getDBTypeFactory();
		QuotedIDFactory idfac =  metadata.getDBParameters().getQuotedIDFactory();

		RDBMetadataLoader metadataLoader;
		String productName = metadata.getDBParameters().getDbmsProductName();
		if (productName.contains("Oracle"))
			metadataLoader = new OracleJDBCRDBMetadataLoader(conn, idfac, dbTypeFactory);
		else if (productName.contains("DB2"))
			metadataLoader = new DB2RDBMetadataLoader(conn, idfac, dbTypeFactory);
		else if (productName.contains("SQL Server"))
			metadataLoader = new MSSQLDBMetadataLoader(conn, idfac, dbTypeFactory);
		else if (productName.contains("MySQL"))
			metadataLoader = new MySQLDBMetadataLoader(conn, idfac, dbTypeFactory);
		else
			metadataLoader = new JDBCRDBMetadataLoader(conn, idfac, dbTypeFactory);

		ImmutableList<RelationID> seedRelationIds = (realTables == null || realTables.isEmpty())
			? metadataLoader.getRelationIDs()
			: realTables.stream()
				.map(metadataLoader::getRelationCanonicalID)
				.collect(ImmutableCollectors.toList());

        List<DatabaseRelationDefinition> extractedRelations2 = new ArrayList<>();
		for (RelationID seedId : seedRelationIds) {
			for (RelationDefinition.AttributeListBuilder r : metadataLoader.getRelationAttributes(seedId)) {
				DatabaseRelationDefinition relation = metadata.createDatabaseRelation(r);
				extractedRelations2.add(relation);
			}
		}

		for (DatabaseRelationDefinition relation : extractedRelations2)
			metadataLoader.insertIntegrityConstraints(relation, metadata);
	}
}
