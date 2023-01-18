package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

/**
 * UCanAccess is a JDBC driver for accessing MS Access. It moves Access data in HSQL (with no persistent storage).
 * Does not support schemas and catalogs.
 *
 */
public class UCanAccessDBMetadataProvider extends AbstractDBMetadataProvider {

    @AssistedInject
    protected UCanAccessDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, DefaultDBMetadataProvider::getQuotedIDFactory, coreSingletons);
    }

    @Override
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        return rawIdFactory.createRelationID(rs.getString(tableNameColumn));
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID id) {
        return id;
    }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) {
        return ImmutableList.of(id);
    }

    @Override
    protected String getRelationCatalog(RelationID id) {
        return null;
    }

    @Override
    protected String getRelationSchema(RelationID id) {
        return null;
    }

    @Override
    protected String getRelationName(RelationID id) {
        return id.getComponents().get(TABLE_INDEX).getName();
    }

    /**
     * TODO: check if % should be escaped (_ should not).
     */
    @Nullable
    @Override
    protected String escapeRelationIdComponentPattern(@Nullable String s) {
        return s;
    }
}
