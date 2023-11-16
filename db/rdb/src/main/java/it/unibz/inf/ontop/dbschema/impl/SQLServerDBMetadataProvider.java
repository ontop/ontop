package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public class SQLServerDBMetadataProvider extends AbstractDBMetadataProvider {

    protected static final int SCHEMA_INDEX = 1;
    protected static final int CATALOG_INDEX = 2;

    private final QuotedID defaultCatalog;
    private final Optional<QuotedID> defaultSchema;

    @AssistedInject
    SQLServerDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new SQLServerQuotedIDFactory(), coreSingletons);
        try {
            String defaultCatalogString = connection.getCatalog();
            String defaultSchemaString = connection.getSchema(); // can be null

            if (defaultCatalogString == null)
                throw new MetadataExtractionException("Unable to obtain the default catalog: make sure the connection URL is complete: " + defaultCatalogString + " " + defaultSchemaString);

            RelationID id = rawIdFactory.createRelationID(defaultCatalogString, defaultSchemaString, "DUMMY");
            defaultCatalog = id.getComponents().get(defaultSchemaString == null ? SCHEMA_INDEX : CATALOG_INDEX);
            defaultSchema = defaultSchemaString == null ? Optional.empty() : Optional.of(id.getComponents().get(SCHEMA_INDEX));
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }



    private static final ImmutableSet<String> IGNORED_SCHEMAS = ImmutableSet.of("sys", "INFORMATION_SCHEMA");

    @Override
    protected boolean isRelationExcluded(RelationID id) {
        return IGNORED_SCHEMAS.contains(getRelationSchema(id));
    }

    @Override
    protected @Nullable String escapeRelationIdComponentPattern(@Nullable String s) {
        return s;
    }

    @Override
    protected String makeQueryMinimizeResultSet(String query) {
        return String.format("SELECT * FROM (%s) subQ ORDER BY (SELECT NULL) OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY", query);
    }


    @Override
    protected RelationID getCanonicalRelationId(RelationID id) throws MetadataExtractionException {
        switch (id.getComponents().size()) {
            case CATALOG_INDEX:
                return new RelationIDImpl(ImmutableList.of(
                        id.getComponents().get(TABLE_INDEX),
                        id.getComponents().get(SCHEMA_INDEX),
                        defaultCatalog));
            case SCHEMA_INDEX:
                return new RelationIDImpl(ImmutableList.of(
                        id.getComponents().get(TABLE_INDEX),
                        defaultSchema.orElseThrow(() -> new MetadataExtractionException("There is no default schema for " + id)),
                        defaultCatalog));
            default:
                return id;
        }
    }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) {
        if (defaultCatalog.equals(id.getComponents().get(CATALOG_INDEX))) {
            RelationID schemaTableId = new RelationIDImpl(id.getComponents().subList(TABLE_INDEX, CATALOG_INDEX));
            if (defaultSchema.filter(d -> d.equals(id.getComponents().get(SCHEMA_INDEX))).isPresent())
                return ImmutableList.of(id.getTableOnlyID(), schemaTableId, id);
            return ImmutableList.of(schemaTableId, id);
        }
        return ImmutableList.of(id);
    }

    @Override
    protected String getRelationCatalog(RelationID id) { return id.getComponents().get(CATALOG_INDEX).getName(); }

    @Override
    protected String getRelationSchema(RelationID id) { return id.getComponents().get(SCHEMA_INDEX).getName(); }

    @Override
    protected String getRelationName(RelationID id) { return id.getComponents().get(TABLE_INDEX).getName(); }

    @Override
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        return rawIdFactory.createRelationID(rs.getString(catalogNameColumn), rs.getString(schemaNameColumn), rs.getString(tableNameColumn));
    }

    /*
                "SELECT DB_NAME() AS TABLE_CAT, SCHEMA_NAME() AS TABLE_SCHEM");
        https://msdn.microsoft.com/en-us/library/ms175068.aspx
        https://docs.microsoft.com/en-us/sql/t-sql/functions/schema-name-transact-sql
        https://docs.microsoft.com/en-us/sql/t-sql/functions/db-name-transact-sql

          return "SELECT TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME " +
					"FROM INFORMATION_SCHEMA.TABLES " +
					"WHERE TABLE_TYPE='BASE TABLE' OR TABLE_TYPE='VIEW'";
    */
}
