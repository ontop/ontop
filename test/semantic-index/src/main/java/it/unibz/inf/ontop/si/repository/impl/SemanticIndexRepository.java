package it.unibz.inf.ontop.si.repository.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.si.impl.LoadingConfiguration;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.spec.ontology.*;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;


public class SemanticIndexRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticIndexRepository.class);
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    private final LoadingConfiguration loadingConfiguration;

    private final IRIDictionaryImpl iriDictionary;

    private final ClassifiedTBox tbox;

    private final SemanticIndex semanticIndex;

    private final RepositoryTableManager views;

    private final MappingProvider mapping;

    public SemanticIndexRepository(ClassifiedTBox tbox, LoadingConfiguration loadingConfiguration) {
        this.tbox = tbox;
        this.loadingConfiguration = loadingConfiguration;

        iriDictionary = new IRIDictionaryImpl();
        semanticIndex = new SemanticIndex(tbox);

        TypeFactory typeFactory = loadingConfiguration.getTypeFactory();
        views = new RepositoryTableManager(typeFactory);

        mapping = new MappingProvider(iriDictionary, loadingConfiguration);

        LOGGER.warn("Semantic index mode initializing: \nString operation over URI are not supported in this mode ");
    }

    public String getJdbcUrl() { return loadingConfiguration.getJdbcUrl(); }

    public String getUser() { return DEFAULT_USER; }

    public String getPassword(){ return DEFAULT_PASSWORD; }

    public String getJdbcDriver() {
        return loadingConfiguration.getJdbcDriver();
    }

    public Connection createConnection() throws SemanticIndexException {

        try {
            Connection localConnection = DriverManager.getConnection(getJdbcUrl(), getUser(), getPassword());
            // Creating the ABox repository
            createDBSchemaAndInsertMetadata(localConnection);
            return localConnection;
        }
        catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    public SQLPPMapping createMappings() {

        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .build();
        SpecificationFactory specificationFactory = defaultConfiguration.getInjector().getInstance(SpecificationFactory.class);
        PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());

        ImmutableList<SQLPPTriplesMap> mappingAxioms = mapping.getMappings(tbox, semanticIndex, views);

        return new SQLPPMappingImpl(mappingAxioms, prefixManager);
    }

    public int insertData(Connection connection, Iterator<RDFFact> iterator) throws SQLException {
        return insertData(connection, iterator, 5000, 500);
    }

    private void createDBSchemaAndInsertMetadata(Connection conn) throws SQLException {
        if (views.isDBSchemaDefined(conn)) {
            LOGGER.debug("Schema already exists. Skipping creation");
            return;
        }

        LOGGER.debug("Creating data tables");
        try (Statement st = conn.createStatement()) {
            semanticIndex.init(st);
            views.init(st);
            st.executeBatch();
        }

        LOGGER.debug("Inserting semantic index metadata.");
        boolean commitval = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            semanticIndex.store(conn);
            views.store(conn);
            conn.commit();
        }
        catch (SQLException e) {
            conn.rollback(); // If there is a big error, restore everything as it was
        }
        finally {
            conn.setAutoCommit(commitval);
        }
    }

    private int insertData(Connection conn, Iterator<RDFFact> data, int commitLimit, int batchLimit) throws SQLException {
        LOGGER.debug("Inserting data into DB");

        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        // For counting the insertion
        int success = 0;
        Map<IRI, Integer> failures = new HashMap<>();

        int batchCount = 0;
        int commitCount = 0;

        try (BatchProcessor batch = new BatchProcessor(conn)) {
            while (data.hasNext()) {
                RDFFact ax = data.next();

                batchCount++;
                commitCount++;

                try {
                    batch.process(ax);
                    success++;
                }
                catch (Exception e) {
                    IRI iri = Optional.of(ax.getClassOrProperty())
                            .filter(c -> c instanceof IRIConstant)
                            .map(c -> (IRIConstant) c)
                            .orElseGet(ax::getProperty)
                            .getIRI();
                    int counter = failures.getOrDefault(iri, 0);
                    failures.put(iri, counter + 1);
                }

                // Check if the batch count is already in the batch limit
                if (batchCount == batchLimit) {
                    batch.execute();
                    batchCount = 0; // reset the counter
                }

                // Check if the commit count is already in the commit limit
                if (commitCount == commitLimit) {
                    conn.commit();
                    commitCount = 0; // reset the counter
                }
            }

            // Execute the rest of the batch
            batch.execute();
            // Commit the rest of the batch insert
            conn.commit();
        }

        conn.setAutoCommit(oldAutoCommit);

        LOGGER.debug("Total successful insertions: " + success + ".");
        int totalFailures = 0;
        for (Map.Entry<IRI, Integer> entry : failures.entrySet()) {
            LOGGER.warn("Failed to insert data for predicate {} ({} tuples).", entry.getKey(), entry.getValue());
            totalFailures += entry.getValue();
        }
        if (totalFailures > 0)
            LOGGER.warn("Total failed insertions: " + totalFailures + ". (REASON: datatype mismatch between the ontology and database).");

        return success;
    }


    private final class BatchProcessor implements AutoCloseable {
        private final Connection conn;
        private final Map<RepositoryTableSlice.Identifier, PreparedStatement> stmMap;

        BatchProcessor(Connection conn) throws SQLException {
            this.conn = conn;
            stmMap = new HashMap<>();
        }

        void process(RDFFact ax) throws SQLException {
            if (ax.isClassAssertion() && (ax.getObject() instanceof IRIConstant)) {
                IRI classIRI = ((IRIConstant) ax.getObject()).getIRI();
                OClass cls0 = tbox.classes().get(classIRI);
                // replace concept by the canonical representative (which must be a concept name)
                OClass cls = (OClass) tbox.classesDAG().getCanonicalForm(cls0);
                process(cls, ax.getSubject());
            }
            else {
                RDFConstant object = ax.getObject();
                IRI propertyIri = ax.getProperty().getIRI();

                if (object instanceof ObjectConstant) {
                    ObjectPropertyExpression ope0 = tbox.objectProperties().get(propertyIri);
                    if (ope0.isInverse())
                        throw new RuntimeException("INVERSE PROPERTIES ARE NOT SUPPORTED IN ABOX:" + ax);
                    ObjectPropertyExpression ope = tbox.objectPropertiesDAG().getCanonicalForm(ope0);
                    if (ope.isInverse())
                        process(ope.getInverse(), (ObjectConstant) object, ax.getSubject());
                    else
                        process(ope, ax.getSubject(), (ObjectConstant) object);
                }
                else if (object instanceof RDFLiteralConstant) {
                    DataPropertyExpression dpe0 = tbox.dataProperties().get(propertyIri);
                    // replace the property by its canonical representative
                    DataPropertyExpression dpe = tbox.dataPropertiesDAG().getCanonicalForm(dpe0);
                    process(dpe, ax.getSubject(), (RDFLiteralConstant) ax.getObject());
                }
            }
        }

        void process(OClass cls, ObjectConstant c1) throws SQLException {
            int idx = semanticIndex.getRange(cls).getIndex();

            int uriId = getObjectConstantUriId(c1);

            RepositoryTableSlice view =  views.getView(c1.getType());
            PreparedStatement stm = getPreparedStatement(view);
            stm.setInt(1, uriId);
            stm.setInt(2, idx);
            stm.addBatch();

            // Register non emptiness
            view.addIndex(idx);
        }

        void process(ObjectPropertyExpression ope, ObjectConstant o1, ObjectConstant o2) throws SQLException {
            int	idx = semanticIndex.getRange(ope).getIndex();

            int uriId1 = getObjectConstantUriId(o1);
            int uriId2 = getObjectConstantUriId(o2);

            RepositoryTableSlice view = views.getView(o1.getType(), o2.getType());
            PreparedStatement stm = getPreparedStatement(view);
            stm.setInt(1, uriId1);
            stm.setInt(2, uriId2);
            stm.setInt(3, idx);
            stm.addBatch();

            // Register non emptiness
            view.addIndex(idx);
        }


        void process(DataPropertyExpression dpe, ObjectConstant subject, RDFLiteralConstant object) throws SQLException {
            int idx = semanticIndex.getRange(dpe).getIndex();

            int uriId = getObjectConstantUriId(subject);

            RepositoryTableSlice view =  views.getView(subject.getType(), object.getType());
            PreparedStatement stm = getPreparedStatement(view);
            stm.setInt(1, uriId);

            IRI typeIri = object.getType().getIRI();
            RepositoryTableManager.PreparedStatementInsertAction insertStmValue = RepositoryTableManager.DATA_PROPERTY_TABLE_INSERT_STM_MAP.get(typeIri);
            if (insertStmValue != null) {
                insertStmValue.setValue(stm, object);
            }
            else {
                LOGGER.warn("Ignoring assertion (unknown datatype): {} {} {}", dpe, subject, object);
                return;
            }

            stm.setInt(3, idx);
            stm.addBatch();

            // register non-emptiness
            view.addIndex(idx);
        }

        PreparedStatement getPreparedStatement(RepositoryTableSlice view) throws SQLException {
            PreparedStatement stm = stmMap.get(view.getId());
            if (stm == null) {
                stm = conn.prepareStatement(view.getINSERT());
                stmMap.put(view.getId(), stm);
            }
            return stm;
        }

        int getObjectConstantUriId(ObjectConstant c) throws SQLException {
            String uri = (c instanceof BNode) ? ((BNode) c).getInternalLabel() : ((IRIConstant) c).getIRI().getIRIString();

            return iriDictionary.getIdOrCreate(uri, null);
        }

        void execute() throws SQLException {
            for (PreparedStatement stm : stmMap.values()) {
                stm.executeBatch();
                stm.clearBatch();
            }
        }

        @Override
        public void close() throws SQLException {
            for (PreparedStatement stm : stmMap.values())
                stm.close();
        }
    }
}
