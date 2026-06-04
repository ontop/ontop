package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class AthenaDBMetadataProvider extends TrinoDBMetadataProvider {

    @AssistedInject
    AthenaDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, coreSingletons);
    }

    /**
     * System tables are only listed when querying information_schema
     * @return
     * @throws SQLException
     */
    @Override
    protected ResultSet getRelationIDsResultSet() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.closeOnCompletion();
        return stmt.executeQuery("SELECT TABLE_CATALOG AS TABLE_CAT, TABLE_SCHEMA AS TABLE_SCHEM, TABLE_NAME " +
                "FROM INFORMATION_SCHEMA.TABLES");
    }

    /**
     * Simba's Athena JDBC driver implements DatabaseMetaData.getColumns with a
     * case-sensitive comparison on the catalog argument, even though Athena SQL itself is
     * case-insensitive. We bypass the driver and query INFORMATION_SCHEMA.COLUMNS directly.
     * The TABLE_CAT, TABLE_SCHEM and TABLE_NAME stored in INFORMATION_SCHEMA.COLUMNS are normalized
     * to lowercase, which can differ from the case of the canonical RelationID (e.g. when the catalog
     * has been padded in from the JDBC URL's Catalog=... parameter). We return the bound parameters instead
     * of the normalized ones.
     */
    @Override
    protected ResultSet getColumnsResultSet(RelationID id) throws SQLException {
        String catalog = getRelationCatalog(id);
        String schema = getRelationSchema(id);
        String table = getRelationName(id);
        PreparedStatement st = connection.prepareStatement(
                "SELECT ? AS TABLE_CAT, " +
                        "       ? AS TABLE_SCHEM, " +
                        "       ? AS TABLE_NAME, " +
                        "       column_name AS COLUMN_NAME, " +
                        "       data_type   AS TYPE_NAME, " +
                        // TYPE_NAME already carries parameters (varchar(100), decimal(38,2)), DATA_TYPE=OTHER avoids extractSQLTypeName re-appending precision.
                        "       CAST(" + Types.OTHER + " AS INTEGER) AS DATA_TYPE, " +
                        // COLUMN_SIZE and DECIMAL_DIGITS are placeholders
                        "       CAST(0 AS INTEGER) AS COLUMN_SIZE, " +
                        "       CAST(0 AS INTEGER) AS DECIMAL_DIGITS, " +
                        "       CASE WHEN is_nullable = 'YES' THEN 1 ELSE 0 END AS NULLABLE " +
                        "FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE LOWER(table_catalog) = LOWER(?) " +
                        "  AND LOWER(table_schema)  = LOWER(?) " +
                        "  AND LOWER(table_name)    = LOWER(?) " +
                        "ORDER BY ordinal_position");
        st.closeOnCompletion();
        st.setString(1, catalog);
        st.setString(2, schema);
        st.setString(3, table);
        st.setString(4, catalog);
        st.setString(5, schema);
        st.setString(6, table);
        return st.executeQuery();
    }

    @Override
    protected boolean isRelationExcluded(RelationID id) {
        return getRelationSchema(id).equals("information_schema");
    }
}
