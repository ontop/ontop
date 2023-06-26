package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.assertEquals;

public class AbstractOWLAPITest {

    private static final String URL_PREFIX = "jdbc:h2:mem:";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOWLAPITest.class);
    private static Connection SQL_CONNECTION;
    private static OntopOWLConnection CONNECTION;

    protected static void initOBDA(String createDbFile, String obdaFile)
            throws SQLException, IOException {
        initOBDA(createDbFile, obdaFile, null, null);
    }

    protected static void initOBDA(String createDbFile, String obdaFile, @Nullable String ontologyFile)
            throws SQLException, IOException {
        initOBDA(createDbFile, obdaFile, ontologyFile, null);
    }

    protected static void initOBDA(String createDbFile, String obdaFile, @Nullable String ontologyFile,
                                   @Nullable String propertiesFile)
            throws SQLException, IOException {
        String jdbcUrl = URL_PREFIX + UUID.randomUUID();

        SQL_CONNECTION = DriverManager.getConnection(jdbcUrl, USER, PASSWORD);
        executeFromFile(SQL_CONNECTION, AbstractOWLAPITest.class.getResource(createDbFile).getPath());

        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(AbstractOWLAPITest.class.getResource(obdaFile).getPath())
                .jdbcUrl(jdbcUrl)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode();

        if (ontologyFile != null)
            builder.ontologyFile(AbstractOWLAPITest.class.getResource(ontologyFile).getPath());

        if (propertiesFile != null)
            builder.propertyFile(AbstractOWLAPITest.class.getResource(propertiesFile).getPath());

        OntopSQLOWLAPIConfiguration config = builder.build();

        OntopOWLEngine reasoner = new SimpleOntopOWLEngine(config);

        CONNECTION = reasoner.getConnection();
    }

    protected static void initR2RML(String createDbFile, String r2rmlFile, String ontologyFile)
            throws SQLException, IOException {
        String jdbcUrl = URL_PREFIX + UUID.randomUUID();

        SQL_CONNECTION = DriverManager.getConnection(jdbcUrl, USER, PASSWORD);
        executeFromFile(SQL_CONNECTION, AbstractOWLAPITest.class.getResource(createDbFile).getPath());

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .r2rmlMappingFile(AbstractOWLAPITest.class.getResource(r2rmlFile).getPath())
                .ontologyFile(AbstractOWLAPITest.class.getResource(ontologyFile).getPath())
                .jdbcUrl(jdbcUrl)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode()
                .build();

        OntopOWLEngine reasoner = new SimpleOntopOWLEngine(config);

        CONNECTION = reasoner.getConnection();
    }

    protected static void release() throws OWLException, SQLException {
        CONNECTION.close();
        SQL_CONNECTION.close();
    }

    protected void checkReturnedValues(String query, String var, ImmutableList<String> expectedValues) throws Exception {
        OWLStatement st = CONNECTION.createStatement();

        ImmutableList.Builder<String> returnedValueBuilder = ImmutableList.builder();
        TupleOWLResultSet rs = st.executeSelectQuery(query);

        int i = 0;
        while (rs.hasNext()) {
            final OWLBindingSet bindingSet = rs.next();
            OWLObject value = bindingSet.getOWLObject(var);
            String stringValue = getStringValue(value);
            if (stringValue != null)
                returnedValueBuilder.add(stringValue);
            i++;
        }

        ImmutableList<String> returnedValues = returnedValueBuilder.build();
        assertEquals(expectedValues, returnedValues);
        assertEquals(expectedValues.size(), i); // required due to possible nulls
    }

    protected String checkReturnedValuesAndReturnSql(String query, String var, List<String> expectedValues) throws Exception {
        OntopOWLStatement st = CONNECTION.createStatement();

        int i = 0;
        List<String> returnedValues = new ArrayList<>();
        IQ executableQuery = st.getExecutableQuery(query);
        String sql = Optional.of(executableQuery.getTree())
                .filter(t -> t instanceof UnaryIQTree)
                .map(t -> ((UnaryIQTree) t).getChild().getRootNode())
                .filter(n -> n instanceof NativeNode)
                .map(n -> ((NativeNode) n).getNativeQueryString())
                .orElseThrow(() -> new RuntimeException("Cannot extract the SQL query from\n" + executableQuery));
        TupleOWLResultSet rs = st.executeSelectQuery(query);
        while (rs.hasNext()) {
            final OWLBindingSet bindingSet = rs.next();
            OWLObject value = bindingSet.getOWLObject(var);
            String stringValue = getStringValue(value);
            if (stringValue != null) {
                returnedValues.add(stringValue);
                LOGGER.debug(stringValue);
            }
            else {
                returnedValues.add("UNBOUND");
                LOGGER.debug("UNBOUND");
            }
            i++;
        }
        assertEquals(expectedValues, returnedValues);
        assertEquals(expectedValues.size(), i); // required due to possible nulls

        LOGGER.debug("SQL: \n" + sql);

        return sql;
    }

    private String getStringValue(OWLObject value) {
        if (value == null)
            return null;

        if (value instanceof OWLLiteral) {
            OWLLiteral literal = (OWLLiteral) value;
            if (literal.getDatatype().isString())
                return "\"" + literal.getLiteral() + "\"^^xsd:string";
            // literal.getLiteral();
            return literal.toString();
        }

        return value.toString();
    }

    protected void checkNumberOfReturnedValues(String query, int expectedNumber) throws Exception {
        try(OntopOWLStatement st = CONNECTION.createStatement()) {
            int i = 0;
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                i++;
            }
            assertEquals(expectedNumber, i);
        }
    }

    protected boolean executeAskQuery(String query) throws Exception {
        try (OWLStatement st = CONNECTION.createStatement()) {
            BooleanOWLResultSet rs = st.executeAskQuery(query);
            return rs.getValue();
        }
    }

    protected String getSqlTranslation(String query) throws Exception {
        OntopOWLStatement st = CONNECTION.createStatement();
        IQ executableQuery = st.getExecutableQuery(query);
        return Optional.of(executableQuery.getTree())
                .filter(t -> t instanceof UnaryIQTree)
                .map(t -> ((UnaryIQTree) t).getChild().getRootNode())
                .filter(n -> n instanceof NativeNode)
                .map(n -> ((NativeNode) n).getNativeQueryString())
                .orElseThrow(() -> new RuntimeException("Cannot extract the SQL query from\n" + executableQuery));
    }

}
