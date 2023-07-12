package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.*;

public class DuckDBDBMetadataProvider extends DefaultSchemaDBMetadataProvider {

    @AssistedInject
    DuckDBDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new DuckDBQuotedIDFactory(), coreSingletons);
    }


    @Override
    protected ResultSet getPrimaryKeysResultSet(String catalog, String schema, String name) throws SQLException {
        PreparedStatement st = metadata.getConnection().prepareStatement(
        "SELECT database_name AS TABLE_CAT, " +
                "schema_name AS TABLE_SCHEM, " +
                "table_name AS TABLE_NAME, " +
                "CONCAT(database_name, '_', schema_name, '_', table_name, '_pk') AS PK_NAME, " +
                "unnest(constraint_column_names) AS COLUMN_NAME, " +
                "generate_subscripts(constraint_column_names, 1) AS KEY_SEQ " +
                "FROM duckdb_constraints " +
                "WHERE " +
                "schema_name = ? AND " +
                "table_name = ? AND " +
                "constraint_type = 'PRIMARY KEY'");

        st.setString(1, schema);
        st.setString(2, name);
        return st.executeQuery();
    }

    @Override
    protected ResultSet getIndexInfo(String catalog, String schema, String name) throws SQLException {
        PreparedStatement st = metadata.getConnection().prepareStatement(
                "SELECT database_name AS TABLE_CAT, " +
                        "schema_name AS TABLE_SCHEM, " +
                        "table_name AS TABLE_NAME, " +
                        "CONCAT(database_name, '_', schema_name, '_', table_name, '_unique_', constraint_index) AS INDEX_NAME, " +
                        "unnest(constraint_column_names) AS COLUMN_NAME, " +
                        "generate_subscripts(constraint_column_names, 1) AS ORDINAL_POSITION, " +
                        "0 AS TYPE, " +
                        "FALSE AS NON_UNIQUE " +
                        "FROM duckdb_constraints " +
                        "WHERE " +
                        "schema_name = ? AND " +
                        "table_name = ? AND " +
                        "constraint_type = 'UNIQUE'");

        st.setString(1, schema);
        st.setString(2, name);
        return st.executeQuery();
    }

    @Override
    protected ResultSet getImportedKeys(String catalog, String schema, String name) throws SQLException {
        PreparedStatement st = metadata.getConnection().prepareStatement(
                "SELECT f.database_name AS FKTABLE_CAT, " +
                        "f.schema_name AS FKTABLE_SCHEM, " +
                        "f.table_name AS FKTABLE_NAME, " +
                        "p.database_name AS PKTABLE_CAT, " +
                        "p.schema_name AS PKTABLE_SCHEM, " +
                        "p.table_name AS PKTABLE_NAME, " +
                        "CONCAT(f.database_name, '_', f.schema_name, '_', f.table_name, '_fk_', f.constraint_index) AS FK_NAME, " +
                        "unnest(f.constraint_column_names) AS FKCOLUMN_NAME, " +
                        "generate_subscripts(f.constraint_column_names, 1) AS KEY_SEQ, " +
                        "unnest(p.constraint_column_names) AS PKCOLUMN_NAME, " +
                        "FROM duckdb_constraints f INNER JOIN duckdb_constraints p " +
                        "ON f.constraint_index = p.constraint_index " +
                        "WHERE " +
                        "f.schema_name = ? AND " +
                        "f.table_name = ? AND " +
                        "p.constraint_type = 'PRIMARY KEY' AND " +
                        "f.constraint_type = 'FOREIGN KEY'");
        st.setString(1, schema);
        st.setString(2, name);
        return st.executeQuery();
    }

    @Override
    protected ResultSet getRelationIDsResultSet() throws SQLException {
        // In duckdb, the type "TABLE" is called "BASE TABLE" instead, so we have to change this method.
        return metadata.getTables(null, null, null, new String[] { "BASE TABLE", "VIEW" });
    }
}
