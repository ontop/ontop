package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public abstract class DefaultSchemaCatalogDBMetadataProvider extends AbstractDBMetadataProvider {

    protected static final int SCHEMA_INDEX = 1;
    protected static final int CATALOG_INDEX = 2;

    private final QuotedID defaultCatalog, defaultSchema;

    DefaultSchemaCatalogDBMetadataProvider(Connection connection, QuotedIDFactoryFactory idFactoryProvider,
                                           CoreSingletons coreSingletons, String sql) throws MetadataExtractionException {
        this(connection, idFactoryProvider, coreSingletons, c -> {
            try (Statement stmt = c.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                rs.next();
                return new String[] {
                        rs.getString("TABLE_CAT"),
                        rs.getString("TABLE_SCHEM"),
                        "DUMMY" };
            }
        });
    }

    DefaultSchemaCatalogDBMetadataProvider(Connection connection, QuotedIDFactoryFactory idFactoryProvider,
                                           CoreSingletons coreSingletons) throws MetadataExtractionException {
        this(connection, idFactoryProvider, coreSingletons,
                c -> new String[] { c.getCatalog(), c.getSchema(), "DUMMY" });
    }

    DefaultSchemaCatalogDBMetadataProvider(Connection connection, QuotedIDFactoryFactory idFactoryProvider,
                                           CoreSingletons coreSingletons, DefaultRelationIdComponentsFactory defaultsFactory) throws MetadataExtractionException {
        super(connection, idFactoryProvider, coreSingletons);
        try {
            String[] defaultRelationComponents = defaultsFactory.getDefaultRelationIdComponents(connection);
            if (defaultRelationComponents == null || defaultRelationComponents.length < 3
                    || defaultRelationComponents[1] == null)
                throw new MetadataExtractionException("Unable to obtain the default schema: make sure the connection URL is complete " + Arrays.toString(defaultRelationComponents));
            if (defaultRelationComponents[0] == null)
                throw new MetadataExtractionException("Unable to obtain the default catalog: make sure the connection URL is complete " + Arrays.toString(defaultRelationComponents));

            RelationID id = rawIdFactory.createRelationID(defaultRelationComponents);
            defaultCatalog = id.getComponents().get(CATALOG_INDEX);
            defaultSchema = id.getComponents().get(SCHEMA_INDEX);
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }


    @Override
    protected RelationID getCanonicalRelationId(RelationID id) {
        switch (id.getComponents().size()) {
            case CATALOG_INDEX:
                return new RelationIDImpl(ImmutableList.of(
                        id.getComponents().get(TABLE_INDEX),
                        id.getComponents().get(SCHEMA_INDEX),
                        defaultCatalog));
            case SCHEMA_INDEX:
                return new RelationIDImpl(ImmutableList.of(
                        id.getComponents().get(TABLE_INDEX),
                        defaultSchema,
                        defaultCatalog));
            default:
                return id;
        }
    }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) {
        if (defaultCatalog.equals(id.getComponents().get(CATALOG_INDEX))) {
            RelationID schemaTableId = new RelationIDImpl(id.getComponents().subList(TABLE_INDEX, CATALOG_INDEX));
            if (defaultSchema.equals(id.getComponents().get(SCHEMA_INDEX)))
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
}
