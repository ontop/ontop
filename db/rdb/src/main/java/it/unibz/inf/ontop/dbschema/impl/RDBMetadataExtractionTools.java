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
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.sql.*;
import java.util.Optional;

/**
 * Retrieves the database metadata (table schema and database constraints)
 *
 * @author Roman Kontchakov
 *
 */

public class RDBMetadataExtractionTools {

	public static MetadataProvider getMetadataProvider(Connection connection, DBTypeFactory dbTypeFactory) throws MetadataExtractionException {
		try {
			DatabaseMetaData md = connection.getMetaData();
			String productName = md.getDatabaseProductName();

			if (productName.contains("Oracle"))
				return new OracleDBMetadataProvider(connection, dbTypeFactory);
			else if (productName.contains("DB2"))
				return new DB2DBMetadataProvider(connection, dbTypeFactory);
			else if (productName.contains("SQL Server"))
				return new SQLServerDBMetadataProvider(connection, dbTypeFactory);
			else if (productName.contains("MySQL"))
				return new MySQLDBMetadataProvider(connection, dbTypeFactory);
			else if (productName.contains("PostgreSQL"))
				return new PostgreSQLDBMetadataProvider(connection, dbTypeFactory);
			else
				return new DefaultDBMetadataProvider(connection, dbTypeFactory);
		}
		catch (SQLException e) {
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Retrieves the database metadata (table schema and database constraints)
	 *
	 * This method either uses the given list of tables or
	 *    if it is null then it retrieves all the complete list of tables from
	 *    the connection metadata
	 * @return
	 */
	public static ImmutableDBMetadataImpl createImmutableMetadata(MetadataProvider metadataLoader) throws MetadataExtractionException {

		ImmutableList.Builder<RelationDefinition> extractedRelations = ImmutableList.builder();
		for (RelationID id : metadataLoader.getRelationIDs())
			extractedRelations.add(metadataLoader.getRelation(id));

		ImmutableDBMetadataImpl metadata = new ImmutableDBMetadataImpl(metadataLoader.getDBParameters(), extractedRelations.build());
		metadataLoader.insertIntegrityConstraints(metadata);
		return metadata;
	}
}
