package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.RelationNotFoundInMetadataException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.spec.sqlparser.ApproximateSelectQueryAttributeExtractor;
import it.unibz.inf.ontop.spec.sqlparser.DefaultSelectQueryAttributeExtractor;
import it.unibz.inf.ontop.spec.sqlparser.JSqlParserTools;
import it.unibz.inf.ontop.spec.sqlparser.ParserViewDefinition;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.TokenMgrException;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractDBMetadataProvider implements DBMetadataProvider {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractDBMetadataProvider.class);

    protected final Connection connection;
    protected final DBParameters dbParameters;
    protected final DatabaseMetaData metadata;
    protected final String escape;

    protected final QuotedIDFactory rawIdFactory;
    private final CoreSingletons coreSingletons;
    private final DBTypeFactory dbTypeFactory;
    private final OntopOBDASettings settings;

    protected interface QuotedIDFactoryFactory {
        QuotedIDFactory create(DatabaseMetaData m) throws SQLException;
    }

    protected interface DefaultRelationIdComponentsFactory {
        String[] getDefaultRelationIdComponents(Connection c) throws SQLException;
    }

    AbstractDBMetadataProvider(Connection connection, QuotedIDFactoryFactory idFactoryProvider,
                               CoreSingletons coreSingletons) throws MetadataExtractionException {
        try {
            this.connection = connection;
            this.metadata = connection.getMetaData();
            this.escape = metadata.getSearchStringEscape();
            QuotedIDFactory idFactory = idFactoryProvider.create(metadata);
            this.rawIdFactory = new RawQuotedIDFactory(idFactory);
            this.dbParameters = new BasicDBParametersImpl(metadata.getDriverName(),
                    metadata.getDriverVersion(),
                    metadata.getDatabaseProductName(),
                    metadata.getDatabaseProductVersion(),
                    idFactory,
                    coreSingletons);
            this.coreSingletons = coreSingletons;
            this.dbTypeFactory = coreSingletons.getTypeFactory().getDBTypeFactory();

            OntopModelSettings modelSettings = coreSingletons.getSettings();
            // HACK! Done so as to avoid changing the constructor of this highly-derived class.
            if (modelSettings instanceof OntopOBDASettings) {
                this.settings = (OntopOBDASettings) modelSettings;
            }
            else
                throw new MinorOntopInternalBugException("Was expecting the settings being an instance of OntopOBDASettings");
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() { return dbParameters.getQuotedIDFactory(); }

    @Override
    public DBParameters getDBParameters() { return dbParameters; }

    @Override
    public void normalizeAndOptimizeRelations(List<NamedRelationDefinition> relationDefinitions) {
        // Does nothing
    }


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

    protected @Nullable String escapeRelationIdComponentPattern(@Nullable String s) {
        return (s == null || escape == null)
                ? s
                : s.replace("_", escape + "_")
                    .replace("%", escape + "%");
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id0) throws MetadataExtractionException {
        DBTypeFactory dbTypeFactory = dbParameters.getDBTypeFactory();
        RelationID id = getCanonicalRelationId(id0);
        try (ResultSet rs = metadata.getColumns(
                getRelationCatalog(id), // catalog is not escaped
                escapeRelationIdComponentPattern(getRelationSchema(id)),
                escapeRelationIdComponentPattern(getRelationName(id)),
                null)) {
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

                String sqlTypeName = extractSQLTypeName(typeName, rs.getInt("DATA_TYPE"), columnSize,
                        () -> rs.getInt("DECIMAL_DIGITS"));
                builder.addAttribute(attributeId, termType, sqlTypeName, isNullable);
            }

            if (relations.entrySet().size() == 1) {
                Map.Entry<RelationID, RelationDefinition.AttributeListBuilder> r = relations.entrySet().iterator().next();
                return new DatabaseTableDefinition(getAllIDs(r.getKey()), r.getValue());
            }
            throw relations.isEmpty()
                    ? new RelationNotFoundInMetadataException(id, getRelationIDs())
                    : new MetadataExtractionException("Cannot resolve ambiguous relation id: " + id + ": " + relations.keySet());
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    protected String extractSQLTypeName(String typeName, int jdbcType, int columnSize,
                                        PrecisionSupplier precisionSupplier) throws SQLException {
        switch (jdbcType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
                return (columnSize != 0) ? typeName + "(" + columnSize + ")" : typeName;
            case Types.DECIMAL:
            case Types.NUMERIC:
                int decimalDigits = precisionSupplier.getPrecision();
                if (columnSize == 0)
                    return typeName;
                else if (decimalDigits == 0)
                    return typeName + "(" + columnSize + ")";
                else
                    return typeName + "(" + columnSize + ", " + decimalDigits + ")";
            default:
                return typeName;
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
            List<String> columnsNotFound = new ArrayList<>();
            String indexId = null;
            while (rs.next()) {
                RelationID extractedId = getRelationID(rs, "TABLE_CAT", "TABLE_SCHEM","TABLE_NAME");
                checkSameRelationID(extractedId, id);

                // TYPE: tableIndexStatistic - this identifies table statistics that are returned in conjunction with a table's index descriptions
                //       tableIndexClustered - this is a clustered index
                //       tableIndexHashed - this is a hashed index
                //       tableIndexOther (all are static final int in DatabaseMetaData)
                if (rs.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                    createIndex(id, builder, indexId, columnsNotFound);
                    builder = null;
                    continue;
                }
                if (rs.getShort("ORDINAL_POSITION") == 1) {
                    createIndex(id, builder, indexId, columnsNotFound);

                    if (!rs.getBoolean("NON_UNIQUE")) {
                        indexId = rs.getString("INDEX_NAME");
                        builder = UniqueConstraint.builder(relation, indexId);
                        columnsNotFound.clear();
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
                            columnsNotFound.add(rawIdFactory.createAttributeID(rs.getString("COLUMN_NAME")).getName());
                        }
                    }
                }
            }
            createIndex(id, builder, indexId, columnsNotFound);
        }
    }

    private static void createIndex(RelationID id, UniqueConstraint.Builder builder, String indexId, List<String> columnsNotFound) {
        if (builder != null) {
            if (columnsNotFound.isEmpty())
                builder.build();
            else
                LOGGER.error("WARNING: column{} {} not found for the unique index {} (table {}): the constraint will not be used in optimizations.",
                        columnsNotFound.size() == 1 ? "" : "s", String.join(", ", columnsNotFound), indexId, id);
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

    @Override
    public RelationDefinition getBlackBoxView(String query) throws MetadataExtractionException, InvalidQueryException {
        return settings.allowRetrievingBlackBoxViewMetadataFromDB()
                ? extractBlackBoxViewByConnectingToDB(query)
                : extractBlackBoxViewWithoutConnectingToDB(query);
    }

    protected RelationDefinition extractBlackBoxViewByConnectingToDB(String query) throws MetadataExtractionException {
        try (Statement st = connection.createStatement();
             ResultSet resultSet = st.executeQuery(makeQueryMinimizeResultSet(query))) {

            ResultSetMetaData resultSetMetadata = resultSet.getMetaData();
            int columnCount = resultSetMetadata.getColumnCount();

            RelationDefinition.AttributeListBuilder builder = AbstractRelationDefinition.attributeListBuilder();

            for (int i=1; i <= columnCount; i++) {
                final int index = i;

                QuotedID attributeId = rawIdFactory.createAttributeID(resultSetMetadata.getColumnName(index));
                String typeName = resultSetMetadata.getColumnTypeName(index);

                int columnSize = resultSetMetadata.getColumnDisplaySize(index);
                DBTermType termType = dbTypeFactory.getDBTermType(typeName, columnSize);

                String sqlTypeName = extractSQLTypeName(typeName, resultSetMetadata.getColumnType(index), columnSize,
                        () -> resultSetMetadata.getPrecision(index));

                builder.addAttribute(attributeId, termType, sqlTypeName, true);
            }
            return new ParserViewDefinition(builder, query);

        } catch (SQLException e) {
            throw new MetadataExtractionException("Cannot extract metadata for a black-box view. ", e);
        }
    }

    /**
     * Can be overridden
     */
    protected String makeQueryMinimizeResultSet(String query) {
        return String.format("SELECT * FROM (%s) subQ LIMIT 1", query);
    }

    protected RelationDefinition extractBlackBoxViewWithoutConnectingToDB(String query) throws InvalidQueryException {
        ImmutableList<QuotedID> attributes;
        try {
            DefaultSelectQueryAttributeExtractor sqae = new DefaultSelectQueryAttributeExtractor(this, coreSingletons);
            SelectBody selectBody = JSqlParserTools.parse(query);
            ImmutableMap<QuotedID, ImmutableTerm> attrs = sqae.getRAExpressionAttributes(selectBody).getUnqualifiedAttributes();
            attributes = ImmutableList.copyOf(attrs.keySet());
        } catch (JSQLParserException e) {
            // TODO: LOGGER.warn() should be instead after revising the logging policy
            System.out.println(String.format("FAILED TO PARSE: %s %s", query, getJSQLParserErrorMessage(query, e)));

            ApproximateSelectQueryAttributeExtractor sqae = new ApproximateSelectQueryAttributeExtractor(getQuotedIDFactory());
            attributes = sqae.getAttributes(query);
        } catch (UnsupportedSelectQueryException e) {
            ApproximateSelectQueryAttributeExtractor sqae = new ApproximateSelectQueryAttributeExtractor(getQuotedIDFactory());
            attributes = sqae.getAttributes(query);
        }
        return new ParserViewDefinition(attributes, query, dbTypeFactory);
    }

    private static String getJSQLParserErrorMessage(String sourceQuery, JSQLParserException e) {
        try {
            // net.sf.jsqlparser.parser.TokenMgrException: Lexical error at line 1, column 165.
            if (e.getCause() instanceof TokenMgrException) {
                Pattern pattern = Pattern.compile("at line (\\d+), column (\\d+)");
                Matcher matcher = pattern.matcher(e.getCause().getMessage());
                if (matcher.find()) {
                    int line = Integer.parseInt(matcher.group(1));
                    int col = Integer.parseInt(matcher.group(2));
                    String sourceQueryLine = sourceQuery.split("\n")[line - 1];
                    final int MAX_LENGTH = 40;
                    if (sourceQueryLine.length() > MAX_LENGTH) {
                        sourceQueryLine = sourceQueryLine.substring(sourceQueryLine.length() - MAX_LENGTH);
                        if (sourceQueryLine.length() > 2 * MAX_LENGTH)
                            sourceQueryLine = sourceQueryLine.substring(0, 2 * MAX_LENGTH);
                        col = MAX_LENGTH;
                    }
                    return "FAILED TO PARSE: " + sourceQueryLine + "\n" +
                            Strings.repeat(" ", "FAILED TO PARSE: ".length() + col - 2) + "^\n" + e.getCause();
                }
            }
        }
        catch (Exception e1) {
            // NOP
        }
        return e.getCause().toString();
    }

    protected abstract RelationID getCanonicalRelationId(RelationID id);

    protected abstract ImmutableList<RelationID> getAllIDs(RelationID id);


    protected abstract String getRelationCatalog(RelationID id);

    protected abstract String getRelationSchema(RelationID id);

    protected abstract String getRelationName(RelationID id);

    @FunctionalInterface
    interface PrecisionSupplier {
        int getPrecision() throws SQLException;
    }
}
