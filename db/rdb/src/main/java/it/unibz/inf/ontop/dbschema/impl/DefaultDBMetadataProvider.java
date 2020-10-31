package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.*;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public class DefaultDBMetadataProvider extends AbstractDBMetadataProvider {

    protected final RelationID defaultSchema;

    private static final int SCHEMA_INDEX = 1;
    private static final int CAT_INDEX = 2;

    protected interface DefaultSchemaProvider {
        String getSchema();
        String getCatalog();
    }

    DefaultDBMetadataProvider(Connection connection, QuotedIDFactoryFactory idFactoryProvider, DefaultSchemaProvider defaultSchemaExtractor, TypeFactory typeFactory) throws MetadataExtractionException {
        super(connection, idFactoryProvider, typeFactory);
        try {
            this.defaultSchema = rawIdFactory.createRelationID(
                    defaultSchemaExtractor.getCatalog(),
                    defaultSchemaExtractor.getSchema(),
                    "DUMMY");
            System.out.println("DB-DEFAULTS (" + metadata.getDatabaseProductName() + "): " + defaultSchema);
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    DefaultDBMetadataProvider(Connection connection, DefaultSchemaProvider defaultSchemaExtractor, TypeFactory typeFactory) throws MetadataExtractionException {
        this(connection, DefaultDBMetadataProvider::getQuotedIDFactory, defaultSchemaExtractor, typeFactory);
    }

    @AssistedInject
    DefaultDBMetadataProvider(@Assisted Connection connection, TypeFactory typeFactory) throws MetadataExtractionException {
        this(connection, DefaultDBMetadataProvider::getQuotedIDFactory, new DefaultSchemaProvider() {
            @Override
            public String getSchema() { return null; }
            @Override
            public String getCatalog() { return null; }
        }, typeFactory);
    }

    protected static QuotedIDFactory getQuotedIDFactory(DatabaseMetaData md) throws SQLException {

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

    protected static class QueryBasedDefaultSchemaProvider implements DefaultSchemaProvider {
        private final String catalog, schema;
        QueryBasedDefaultSchemaProvider(Connection connection, String sql) throws MetadataExtractionException {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                rs.next();
                schema = rs.getString("TABLE_SCHEM");
                catalog = rs.getString("TABLE_CAT");
            }
            catch (SQLException e) {
                throw new MetadataExtractionException(e);
            }
        }

        @Override
        public String getSchema() { return schema; }
        @Override
        public String getCatalog() { return catalog; }
    }



    // can be overridden, single usage
    protected boolean isInDefaultSchema(RelationID id) {
        return defaultSchema.getComponents().subList(SCHEMA_INDEX, defaultSchema.getComponents().size())
                .equals(id.getComponents().subList(SCHEMA_INDEX, id.getComponents().size()));
    }
    // can be overridden, single usage
    protected boolean isInDefaultCatalog(RelationID id) {
        return defaultSchema.getComponents().size() > SCHEMA_INDEX
                && id.getComponents().size() > SCHEMA_INDEX
                && defaultSchema.getComponents().subList(CAT_INDEX, defaultSchema.getComponents().size())
                    .equals(id.getComponents().subList(CAT_INDEX, id.getComponents().size()));
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID id) {
        if (id.getComponents().size() >= defaultSchema.getComponents().size())
            return id;

        if (id.getComponents().size() == CAT_INDEX)
            return new RelationIDImpl(ImmutableList.<QuotedID>builder()
                    .add(id.getComponents().get(TABLE_INDEX))
                    .add(id.getComponents().get(SCHEMA_INDEX))
                    .addAll(defaultSchema.getComponents().subList(CAT_INDEX, defaultSchema.getComponents().size()))
                    .build());

        return new RelationIDImpl(ImmutableList.<QuotedID>builder()
                .add(id.getComponents().get(TABLE_INDEX))
                .addAll(defaultSchema.getComponents().subList(SCHEMA_INDEX, defaultSchema.getComponents().size()))
                .build());
    }


    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) {
        return isInDefaultSchema(id)
                ? ImmutableList.of(id.getTableOnlyID(), getSchemaTableID(id), id)
                :  isInDefaultCatalog(id)
                ? ImmutableList.of(getSchemaTableID(id), id)
                : ImmutableList.of(id);
    }


    private RelationID getSchemaTableID(RelationID id) {
        return new RelationIDImpl(id.getComponents().subList(TABLE_INDEX, CAT_INDEX));
    }


    @Override
    protected String getRelationCatalog(RelationID relationID) { return relationID.getComponents().size() > CAT_INDEX  ? relationID.getComponents().get(CAT_INDEX).getName() : null; }

    @Override
    protected String getRelationSchema(RelationID relationID) { return relationID.getComponents().size() > SCHEMA_INDEX  ? relationID.getComponents().get(SCHEMA_INDEX).getName() : null; }

    @Override
    protected String getRelationName(RelationID relationID) { return relationID.getComponents().get(TABLE_INDEX).getName(); }

    @Override
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        return rawIdFactory.createRelationID(rs.getString(catalogNameColumn), rs.getString(schemaNameColumn), rs.getString(tableNameColumn));
    }
}