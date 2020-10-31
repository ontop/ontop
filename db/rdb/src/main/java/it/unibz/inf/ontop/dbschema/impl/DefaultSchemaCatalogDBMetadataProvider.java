package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public abstract class DefaultSchemaCatalogDBMetadataProvider extends AbstractDBMetadataProvider {

    private static final int SCHEMA_INDEX = 1;
    private static final int CATALOG_INDEX = 2;

    private final RelationID defaultSchema;

    DefaultSchemaCatalogDBMetadataProvider(Connection connection, QuotedIDFactoryFactory idFactoryProvider, TypeFactory typeFactory, String sql) throws MetadataExtractionException {
        super(connection, idFactoryProvider, typeFactory);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            defaultSchema = rawIdFactory.createRelationID(rs.getString("TABLE_CAT"), rs.getString("TABLE_SCHEM"), "DUMMY");
            System.out.println("DB-DEFAULTS (" + connection.getMetaData().getDatabaseProductName() + "): " + defaultSchema);
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
                        defaultSchema.getComponents().get(CATALOG_INDEX)));
            case SCHEMA_INDEX:
                return new RelationIDImpl(ImmutableList.of(
                        id.getComponents().get(TABLE_INDEX),
                        defaultSchema.getComponents().get(SCHEMA_INDEX),
                        defaultSchema.getComponents().get(CATALOG_INDEX)));
            default:
                return id;
        }
    }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) {
        if (defaultSchema.getComponents().get(CATALOG_INDEX).equals(id.getComponents().get(CATALOG_INDEX))) {
            RelationID schemaTableId = new RelationIDImpl(id.getComponents().subList(TABLE_INDEX, CATALOG_INDEX));
            if (defaultSchema.getComponents().get(SCHEMA_INDEX).equals(id.getComponents().get(SCHEMA_INDEX)))
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
