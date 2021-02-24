package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.*;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public class DefaultDBMetadataProvider extends AbstractDBMetadataProvider {

    private static final int SCHEMA_INDEX = 1;
    private static final int CATALOG_INDEX = 2;

    @AssistedInject
    DefaultDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, DefaultDBMetadataProvider::getQuotedIDFactory, coreSingletons);
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID id) { return id; }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) { return ImmutableList.of(id); }

    @Override
    protected String getRelationCatalog(RelationID relationID) { return relationID.getComponents().size() > CATALOG_INDEX ? relationID.getComponents().get(CATALOG_INDEX).getName() : null; }

    @Override
    protected String getRelationSchema(RelationID relationID) { return relationID.getComponents().size() > SCHEMA_INDEX  ? relationID.getComponents().get(SCHEMA_INDEX).getName() : null; }

    @Override
    protected String getRelationName(RelationID relationID) { return relationID.getComponents().get(TABLE_INDEX).getName(); }

    @Override
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        return rawIdFactory.createRelationID(rs.getString(catalogNameColumn), rs.getString(schemaNameColumn), rs.getString(tableNameColumn));
    }



    public static QuotedIDFactory getQuotedIDFactory(DatabaseMetaData md) throws SQLException {

        if (md.storesMixedCaseIdentifiers())
            // treat Exareme as a case-sensitive DB engine (like MS SQL Server)
            // "SQL Server" = MS SQL Server
            return new SQLServerQuotedIDFactory();

        else if (md.storesLowerCaseIdentifiers())
            // PostgreSQL treats unquoted identifiers as lower-case
            return new PostgreSQLQuotedIDFactory();

        else if (md.storesUpperCaseIdentifiers())
            // Oracle, DB2, H2, HSQL
            return new SQLStandardQuotedIDFactory();

        // UNKNOWN COMBINATION
        LOGGER.warn("Unknown combination of identifier handling rules: " + md.getDatabaseProductName());
        LOGGER.warn("storesLowerCaseIdentifiers: " + md.storesLowerCaseIdentifiers());
        LOGGER.warn("storesUpperCaseIdentifiers: " + md.storesUpperCaseIdentifiers());
        LOGGER.warn("storesMixedCaseIdentifiers: " + md.storesMixedCaseIdentifiers());
        LOGGER.warn("supportsMixedCaseIdentifiers: " + md.supportsMixedCaseIdentifiers());
        LOGGER.warn("storesLowerCaseQuotedIdentifiers: " + md.storesLowerCaseQuotedIdentifiers());
        LOGGER.warn("storesUpperCaseQuotedIdentifiers: " + md.storesUpperCaseQuotedIdentifiers());
        LOGGER.warn("storesMixedCaseQuotedIdentifiers: " + md.storesMixedCaseQuotedIdentifiers());
        LOGGER.warn("supportsMixedCaseQuotedIdentifiers: " + md.supportsMixedCaseQuotedIdentifiers());
        LOGGER.warn("getIdentifierQuoteString: " + md.getIdentifierQuoteString());

        return new SQLStandardQuotedIDFactory();
    }

}