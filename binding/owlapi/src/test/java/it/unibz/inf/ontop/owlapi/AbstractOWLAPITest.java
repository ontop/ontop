package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected static void initOBDA(String createDbFile, String obdaFile, String ontologyFile)
            throws SQLException, IOException, OWLOntologyCreationException {
        String jdbcUrl = URL_PREFIX + UUID.randomUUID().toString();

        SQL_CONNECTION = DriverManager.getConnection(jdbcUrl, USER, PASSWORD);
        executeFromFile(SQL_CONNECTION, AbstractOWLAPITest.class.getResource(createDbFile).getPath());

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(AbstractOWLAPITest.class.getResource(obdaFile).getPath())
                .ontologyFile(AbstractOWLAPITest.class.getResource(ontologyFile).getPath())
                .jdbcUrl(jdbcUrl)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode()
                .build();

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

        CONNECTION = reasoner.getConnection();
    }

    protected static void release() throws OWLException, SQLException {
        CONNECTION.close();
        SQL_CONNECTION.close();
    }

    protected void checkReturnedValues(String query, ImmutableList<String> expectedVValues) throws Exception {
        OWLStatement st = CONNECTION.createStatement();

        ImmutableList.Builder<String> returnedValueBuilder = ImmutableList.builder();
        TupleOWLResultSet rs = st.executeSelectQuery(query);

        while (rs.hasNext()) {
            final OWLBindingSet bindingSet = rs.next();

            OWLObject value = bindingSet.getOWLObject("v");
            String stringValue = (value instanceof OWLLiteral)
                    ? ((OWLLiteral) value).getLiteral()
                    : (value == null) ? null : value.toString();

            if (stringValue != null)
                returnedValueBuilder.add(stringValue);
        }

        ImmutableList<String> returnedValues = returnedValueBuilder.build();
        assertEquals(String.format("%s instead of \n %s", returnedValues.toString(), expectedVValues.toString()),
                returnedValues, expectedVValues);
    }

    protected String checkReturnedValuesAndReturnSql(String query, List<String> expectedVValues) throws Exception {
        OntopOWLStatement st = CONNECTION.createStatement();
        String sql;

        int i = 0;
        List<String> returnedValues = new ArrayList<>();
        IQ executableQuery = st.getExecutableQuery(query);
        sql = Optional.of(executableQuery.getTree())
                .filter(t -> t instanceof UnaryIQTree)
                .map(t -> ((UnaryIQTree) t).getChild().getRootNode())
                .filter(n -> n instanceof NativeNode)
                .map(n -> ((NativeNode) n).getNativeQueryString())
                .orElseThrow(() -> new RuntimeException("Cannot extract the SQL query from\n" + executableQuery));
        TupleOWLResultSet rs = st.executeSelectQuery(query);
        while (rs.hasNext()) {
            final OWLBindingSet bindingSet = rs.next();
            OWLObject value = bindingSet.getOWLObject("v");
            String stringValue = (value instanceof OWLLiteral)
                    ? ((OWLLiteral) value).getLiteral()
                    : (value == null) ? null : value.toString();
            if (stringValue != null) {
                returnedValues.add(stringValue);
                LOGGER.debug(stringValue);
            }
            i++;
        }
        assertEquals(String.format("%s instead of \n %s", returnedValues.toString(), expectedVValues.toString()), returnedValues, expectedVValues);
        assertEquals(String.format("Wrong size: %d (expected %d)", i, expectedVValues.size()), expectedVValues.size(), i);

        LOGGER.debug("SQL: \n" + sql);

        return sql;
    }
}
