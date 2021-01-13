package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.RelationNotFoundInMetadataException;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDBMetadataProvider implements DBMetadataProvider {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractDBMetadataProvider.class);

    protected final Connection connection;
    protected final DBParameters dbParameters;
    protected final DatabaseMetaData metadata;

    protected final QuotedIDFactory rawIdFactory;

    protected interface QuotedIDFactoryFactory {
        QuotedIDFactory create(DatabaseMetaData m) throws SQLException;
    }

    AbstractDBMetadataProvider(Connection connection, QuotedIDFactoryFactory idFactoryProvider, TypeFactory typeFactory) throws MetadataExtractionException {
        try {
            this.connection = connection;
            this.metadata = connection.getMetaData();
            QuotedIDFactory idFactory = idFactoryProvider.create(metadata);
            this.rawIdFactory = new RawQuotedIDFactory(idFactory);
            this.dbParameters = new BasicDBParametersImpl(metadata.getDriverName(),
                    metadata.getDriverVersion(),
                    metadata.getDatabaseProductName(),
                    metadata.getDatabaseProductVersion(),
                    idFactory,
                    typeFactory.getDBTypeFactory());
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() { return dbParameters.getQuotedIDFactory(); }

    @Override
    public DBParameters getDBParameters() { return dbParameters; }



    protected boolean isRelationExcluded(RelationID id) { return false; }

    protected ResultSet getRelationIDsResultSet() throws SQLException {
        return metadata.getTables(null, null, null, new String[] { "TABLE", "VIEW" });
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        try (ResultSet rs = getRelationIDsResultSet()) {
            ImmutableList.Builder<RelationID> builder = ImmutableList.builder();
            while (rs.next()) {
                RelationID id = getRelationID(rs, "TABLE_CAT", "TABLE_SCHEM","TABLE_NAME");
                if (!isRelationExcluded(id))
                    builder.add(id);
            }
            return builder.build();
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    protected abstract RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColum) throws SQLException;

    // can be overridden, 4 usages
    protected void checkSameRelationID(RelationID extractedId, RelationID givenId) throws MetadataExtractionException {
        if (!extractedId.equals(givenId))
            throw new MetadataExtractionException("Relation IDs mismatch: " + givenId + " v " + extractedId );
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id0) throws MetadataExtractionException {
        DBTypeFactory dbTypeFactory = dbParameters.getDBTypeFactory();
        RelationID id = getCanonicalRelationId(id0);
        try (ResultSet rs = metadata.getColumns(getRelationCatalog(id), getRelationSchema(id), getRelationName(id), null)) {
            Map<RelationID, RelationDefinition.AttributeListBuilder> relations = new HashMap<>();

            while (rs.next()) {
                RelationID extractedId = getRelationID(rs, "TABLE_CAT", "TABLE_SCHEM","TABLE_NAME");
                checkSameRelationID(extractedId, id);

                RelationDefinition.AttributeListBuilder builder = relations.computeIfAbsent(extractedId,
                        i -> DatabaseTableDefinition.attributeListBuilder());

                QuotedID attributeId = rawIdFactory.createAttributeID(rs.getString("COLUMN_NAME"));
                // columnNoNulls, columnNullable, columnNullableUnknown
                boolean isNullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                String typeName = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                DBTermType termType = dbTypeFactory.getDBTermType(typeName, columnSize);

                String sqlTypeName;
                switch (rs.getInt("DATA_TYPE")) {
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.NVARCHAR:
                        sqlTypeName = (columnSize != 0) ? typeName + "(" + columnSize + ")" : typeName;
                        break;
                    case Types.DECIMAL:
                    case Types.NUMERIC:
                        int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                        if (columnSize == 0)
                            sqlTypeName = typeName;
                        else if (decimalDigits == 0)
                            sqlTypeName = typeName + "(" + columnSize + ")";
                        else
                            sqlTypeName = typeName + "(" + columnSize + ", " + decimalDigits + ")";
                        break;
                    default:
                        sqlTypeName = typeName;
                }
                builder.addAttribute(attributeId, termType, sqlTypeName, isNullable);
            }

            if (relations.entrySet().size() == 1) {
                Map.Entry<RelationID, RelationDefinition.AttributeListBuilder> r = relations.entrySet().iterator().next();
                return new DatabaseTableDefinition(getAllIDs(r.getKey()), r.getValue());
            }
            throw relations.isEmpty()
                    ? new RelationNotFoundInMetadataException(id)
                    : new MetadataExtractionException("Cannot resolve ambiguous relation id: " + id + ": " + relations.keySet());
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }


    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
        try {
            insertPrimaryKey(relation);
            insertUniqueAttributes(relation);
            insertForeignKeys(relation, metadataLookup);
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    private void insertPrimaryKey(NamedRelationDefinition relation) throws MetadataExtractionException, SQLException {
        RelationID id = getCanonicalRelationId(relation.getID());
        // Retrieves a description of the given table's primary key columns. They are ordered by COLUMN_NAME (sic!)
        try (ResultSet rs = metadata.getPrimaryKeys(getRelationCatalog(id), getRelationSchema(id), getRelationName(id))) {
            Map<Integer, QuotedID> primaryKeyAttributes = new HashMap<>();
            String currentName = null;
            while (rs.next()) {
                RelationID extractedId = getRelationID(rs, "TABLE_CAT", "TABLE_SCHEM","TABLE_NAME");
                checkSameRelationID(extractedId, id);

                currentName = rs.getString("PK_NAME"); // may be null
                QuotedID attrId = rawIdFactory.createAttributeID(rs.getString("COLUMN_NAME"));
                int seq = rs.getShort("KEY_SEQ");
                primaryKeyAttributes.put(seq, attrId);
            }
            if (!primaryKeyAttributes.isEmpty()) {
                try {
                    // use the KEY_SEQ values to restore the correct order of attributes in the PK
                    UniqueConstraint.Builder builder = UniqueConstraint.primaryKeyBuilder(relation, currentName);
                    for (int i = 1; i <= primaryKeyAttributes.size(); i++)
                        builder.addDeterminant(primaryKeyAttributes.get(i));
                    builder.build();
                }
                catch (AttributeNotFoundException e) {
                    throw new MetadataExtractionException(e);
                }
            }
        }
    }

    private void insertUniqueAttributes(NamedRelationDefinition relation) throws MetadataExtractionException, SQLException {
        RelationID id = getCanonicalRelationId(relation.getID());
        // extracting unique
        try (ResultSet rs = metadata.getIndexInfo(getRelationCatalog(id), getRelationSchema(id), getRelationName(id), true, true)) {
            UniqueConstraint.Builder builder = null;
            while (rs.next()) {
                RelationID extractedId = getRelationID(rs, "TABLE_CAT", "TABLE_SCHEM","TABLE_NAME");
                checkSameRelationID(extractedId, id);

                // TYPE: tableIndexStatistic - this identifies table statistics that are returned in conjunction with a table's index descriptions
                //       tableIndexClustered - this is a clustered index
                //       tableIndexHashed - this is a hashed index
                //       tableIndexOther (all are static final int in DatabaseMetaData)
                if (rs.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                    if (builder != null)
                        builder.build();

                    builder = null;
                    continue;
                }
                if (rs.getShort("ORDINAL_POSITION") == 1) {
                    if (builder != null)
                        builder.build();

                    if (!rs.getBoolean("NON_UNIQUE")) {
                        String name = rs.getString("INDEX_NAME");
                        builder = UniqueConstraint.builder(relation, name);
                    }
                    else
                        builder = null;
                }

                if (builder != null) {
                    QuotedID attrId = rawIdFactory.createAttributeID(rs.getString("COLUMN_NAME"));
                    // ASC_OR_DESC String => column sort sequence, "A" => ascending, "D" => descending,
                    //        may be null if sort sequence is not supported; null when TYPE is tableIndexStatistic
                    // CARDINALITY int => When TYPE is tableIndexStatistic, then this is the number of rows in the table;
                    //                      otherwise, it is the number of unique values in the index.
                    // PAGES int => When TYPE is tableIndexStatisic then this is the number of pages used for the table,
                    //                    otherwise it is the number of pages used for the current index.
                    // FILTER_CONDITION String => Filter condition, if any. (may be null)
                    try {
                        builder.addDeterminant(attrId);
                    }
                    catch (AttributeNotFoundException e) {
                        try {
                            // bug in PostgreSQL JBDC driver: it strips off the quotation marks
                            attrId = rawIdFactory.createAttributeID("\"" + rs.getString("COLUMN_NAME") + "\"");
                            builder.addDeterminant(attrId);
                        }
                        catch (AttributeNotFoundException ex) {
                            throw new MetadataExtractionException(e);
                        }
                    }
                }
            }
            if (builder != null)
                builder.build();
        }
    }

    private void insertForeignKeys(NamedRelationDefinition relation, MetadataLookup dbMetadata) throws MetadataExtractionException, SQLException {
        RelationID id = getCanonicalRelationId(relation.getID());
        try (ResultSet rs = metadata.getImportedKeys(getRelationCatalog(id), getRelationSchema(id), getRelationName(id))) {
            ForeignKeyConstraint.Builder builder = null;
            while (rs.next()) {
                RelationID extractedId = getRelationID(rs, "FKTABLE_CAT", "FKTABLE_SCHEM","FKTABLE_NAME");
                checkSameRelationID(extractedId, id);
                RelationID pkId = getRelationID(rs, "PKTABLE_CAT", "PKTABLE_SCHEM","PKTABLE_NAME");

                try {
                    int seq = rs.getShort("KEY_SEQ");
                    if (seq == 1) {
                        if (builder != null)
                            builder.build();

                        String name = rs.getString("FK_NAME"); // String => foreign key name (may be null)

                        NamedRelationDefinition ref = dbMetadata.getRelation(pkId);

                        builder = ForeignKeyConstraint.builder(name, relation, ref);
                    }
                    if (builder != null) {
                        try {
                            QuotedID attrId = rawIdFactory.createAttributeID(rs.getString("FKCOLUMN_NAME"));
                            QuotedID refAttrId = rawIdFactory.createAttributeID(rs.getString("PKCOLUMN_NAME"));
                            builder.add(attrId, refAttrId);
                        }
                        catch (AttributeNotFoundException e) {
                            throw new MetadataExtractionException(e);
                        }
                    }
                }
                catch (MetadataExtractionException e) {
                    LOGGER.warn("Cannot find table {} for FK {}", pkId, rs.getString("FK_NAME"));
                    builder = null; // do not add this foreign key because there is no table it refers to
                }
            }
            if (builder != null)
                builder.build();
        }
    }

    protected abstract RelationID getCanonicalRelationId(RelationID id);

    protected abstract ImmutableList<RelationID> getAllIDs(RelationID id);


    protected abstract String getRelationCatalog(RelationID id);

    protected abstract String getRelationSchema(RelationID id);

    protected abstract String getRelationName(RelationID id);
}
