package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public abstract class DefaultSchemaDBMetadataProvider extends AbstractDBMetadataProvider {

    protected static final int SCHEMA_INDEX = 1;

    private final QuotedID defaultSchema;

    DefaultSchemaDBMetadataProvider(Connection connection, QuotedIDFactoryFactory idFactoryProvider, CoreSingletons coreSingletons, String sql) throws MetadataExtractionException {
        super(connection, idFactoryProvider, coreSingletons);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            RelationID id = rawIdFactory.createRelationID(rs.getString("TABLE_SCHEM"), "DUMMY");
            defaultSchema = id.getComponents().get(SCHEMA_INDEX);

        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID id) {
        return (id.getComponents().size() > SCHEMA_INDEX)
                ? id
                : new RelationIDImpl(ImmutableList.of(id.getComponents().get(TABLE_INDEX), defaultSchema));
    }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) {
        return defaultSchema.equals(id.getComponents().get(SCHEMA_INDEX))
                ? ImmutableList.of(id.getTableOnlyID(), id)
                : ImmutableList.of(id);
    }

    @Override
    protected String getRelationCatalog(RelationID id) { return null; }

    @Override
    protected String getRelationSchema(RelationID id) { return id.getComponents().get(SCHEMA_INDEX).getName(); }

    @Override
    protected String getRelationName(RelationID id) { return id.getComponents().get(TABLE_INDEX).getName(); }

    @Override
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        return rawIdFactory.createRelationID(rs.getString(schemaNameColumn), rs.getString(tableNameColumn));
    }
}
