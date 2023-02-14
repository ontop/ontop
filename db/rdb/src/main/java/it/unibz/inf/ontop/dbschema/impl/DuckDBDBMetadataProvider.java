package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DuckDBDBMetadataProvider extends DefaultSchemaDBMetadataProvider {

    @AssistedInject
    DuckDBDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new DuckDBQuotedIDFactory(), coreSingletons);
    }


    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
        /* DuckDB does not support access to integrity constraints through the JDBC Metadata.
           However, we can run queries on the database to get that information.
        */
        insertPrimaryKey(relation);
        insertUniqueAttributes(relation);
        insertForeignKeys(relation, metadataLookup);
    }

    private void insertPrimaryKey(NamedRelationDefinition relation) throws MetadataExtractionException {
        RelationID canonicalRelationId = this.getCanonicalRelationId(relation.getID());

        String query = String.format("SELECT LIST_AGGREGATE(constraint_column_names, 'STRING_AGG') FROM duckdb_constraints WHERE " +
                "table_name='%s' and " +
                "schema_name='%s' and " + //TODO if schema is null, we need to use default schema ('main') here
                "constraint_type = 'PRIMARY KEY';",
                canonicalRelationId.getComponents().get(0).getName(),
                canonicalRelationId.getComponents().get(1).getName());

        List<String> primaryKeys = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while(rs.next()) {

                if (primaryKeys.size() > 0)
                    throw new MetadataExtractionException("Two primary keys for the same table " + relation.getID() + "!");

                Arrays.stream(rs.getString(1).split(",")).forEach(s -> primaryKeys.add(s));
            }
        } catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }

        if (!primaryKeys.isEmpty()) {
            try {
                // use the KEY_SEQ values to restore the correct order of attributes in the PK
                UniqueConstraint.Builder builder = UniqueConstraint.primaryKeyBuilder(relation, UUID.randomUUID().toString());
                for (String key : primaryKeys) {
                    QuotedID attrId = rawIdFactory.createAttributeID(key);
                    builder.addDeterminant(attrId);
                }
                builder.build();
            }
            catch (AttributeNotFoundException e) {
                throw new MetadataExtractionException(e);
            }
        }
    }
    private void insertUniqueAttributes(NamedRelationDefinition relation) throws MetadataExtractionException {
        RelationID canonicalRelationId = this.getCanonicalRelationId(relation.getID());

        String query = String.format("SELECT LIST_AGGREGATE(constraint_column_names, 'STRING_AGG'), constraint_text FROM duckdb_constraints WHERE " +
                "table_name='%s' and " +
                "schema_name='%s' and " +
                "constraint_type = 'UNIQUE';",
                canonicalRelationId.getComponents().get(0).getName(),
                canonicalRelationId.getComponents().get(1).getName());

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while(rs.next()) {

                UniqueConstraint.Builder builder = UniqueConstraint.builder(relation, UUID.randomUUID().toString());
                Arrays.stream(rs.getString(1).split(",")).forEach(s -> {
                    try {
                        builder.addDeterminant(rawIdFactory.createAttributeID(s));
                    } catch (AttributeNotFoundException e) {
                        LOGGER.warn("Cannot find attribute of unique constraint " + s);
                    }
                });
                builder.build();

            }
        } catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    private void insertForeignKeys(NamedRelationDefinition relation, MetadataLookup dbMetadata) throws MetadataExtractionException {
        RelationID canonicalRelationId = this.getCanonicalRelationId(relation.getID());
        String query = String.format("" +
                "SELECT a.schema_name as s1, a.table_name as t1, LIST_AGGREGATE(a.constraint_column_names, 'STRING_AGG') as c1, " +
                       "LIST_AGGREGATE(b.constraint_column_names, 'STRING_AGG') AS c2 " +
                "FROM duckdb_constraints a INNER JOIN duckdb_constraints b ON " +
                        "a.constraint_index = b.constraint_index " +
                "WHERE " +
                        "a.constraint_type = 'PRIMARY KEY' and " +
                        "b.constraint_type = 'FOREIGN KEY' and " +
                        "b.table_name='%s' and " +
                        "b.schema_name='%s';",
                canonicalRelationId.getComponents().get(0).getName(),
                canonicalRelationId.getComponents().get(1).getName());

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while(rs.next()) {
                List<String> foreignKeys = new ArrayList<>();
                List<String> primaryKeys = new ArrayList<>();

                Arrays.stream(rs.getString(4).split(",")).forEach(s -> foreignKeys.add(s));
                Arrays.stream(rs.getString(3).split(",")).forEach(s -> primaryKeys.add(s));
                RelationID pkId = getRelationID(rs, null, "s1","t1");
                NamedRelationDefinition ref = dbMetadata.getRelation(pkId);
                ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(UUID.randomUUID().toString(), relation, ref);

                try {
                    for(int i = 0; i < primaryKeys.size(); i++) {
                        QuotedID attrId = rawIdFactory.createAttributeID(foreignKeys.get(i));
                        QuotedID refAttrId = rawIdFactory.createAttributeID(primaryKeys.get(i));
                        builder.add(attrId, refAttrId);
                    }
                }
                catch (AttributeNotFoundException e) {
                    throw new MetadataExtractionException(e);
                }

                builder.build();
            }
        } catch (SQLException e) {
            throw new MetadataExtractionException(e);
        } catch (MetadataExtractionException e) {
            LOGGER.warn("Cannot find table for foreign key.");
        }
    }

    @Override
    protected ResultSet getRelationIDsResultSet() throws SQLException {
        return metadata.getTables(null, null, null, new String[] { "BASE TABLE", "VIEW" });
    }
}
