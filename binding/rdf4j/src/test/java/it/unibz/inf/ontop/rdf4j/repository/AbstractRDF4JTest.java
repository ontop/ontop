package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class AbstractRDF4JTest {

    private static final String URL_PREFIX = "jdbc:h2:mem:";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRDF4JTest.class);
    private static Connection SQL_CONNECTION;
    private static OntopRepositoryConnection REPO_CONNECTION;

    protected static void initOBDA(String dbScriptRelativePath, String obdaRelativePath) throws SQLException, IOException {
        initOBDA(dbScriptRelativePath, obdaRelativePath, null);
    }
    protected static void initOBDA(String dbScriptRelativePath, String obdaRelativePath,
                                   @Nullable String ontologyRelativePath) throws SQLException, IOException {
        initOBDA(dbScriptRelativePath, obdaRelativePath, ontologyRelativePath, null);
    }

    protected static void initOBDA(String dbScriptRelativePath, String obdaRelativePath,
                                   @Nullable String ontologyRelativePath, @Nullable String propertyFile) throws SQLException, IOException {
        initOBDA(dbScriptRelativePath, obdaRelativePath, ontologyRelativePath, propertyFile, null);
    }

    protected static void initOBDA(String dbScriptRelativePath, String obdaRelativePath,
                                   @Nullable String ontologyRelativePath, @Nullable String propertyFile,
                                   @Nullable String viewFile) throws SQLException, IOException {
        initOBDA(dbScriptRelativePath, obdaRelativePath, ontologyRelativePath, propertyFile, viewFile, null);
    }

    protected static void initOBDA(String dbScriptRelativePath, String obdaRelativePath,
                                   @Nullable String ontologyRelativePath, @Nullable String propertyFile,
                                   @Nullable String viewFile, @Nullable String dbMetadataFile) throws SQLException, IOException {
        String jdbcUrl = URL_PREFIX + UUID.randomUUID().toString();

        SQL_CONNECTION = DriverManager.getConnection(jdbcUrl, USER, PASSWORD);

        java.sql.Statement st = SQL_CONNECTION.createStatement();

        FileReader reader = new FileReader(AbstractRDF4JTest.class.getResource(dbScriptRelativePath).getPath());
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        SQL_CONNECTION.commit();

        OntopSQLOWLAPIConfiguration.Builder<? extends OntopSQLOWLAPIConfiguration.Builder> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(AbstractRDF4JTest.class.getResource(obdaRelativePath).getPath())
                .jdbcUrl(jdbcUrl)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode();

        if (ontologyRelativePath != null)
            builder.ontologyFile(AbstractRDF4JTest.class.getResource(ontologyRelativePath).getPath());

        if (propertyFile != null)
            builder.propertyFile(AbstractRDF4JTest.class.getResource(propertyFile).getPath());

        if (viewFile != null)
            builder.ontopViewFile(AbstractRDF4JTest.class.getResource(viewFile).getPath());

        if (dbMetadataFile != null)
            builder.dbMetadataFile(AbstractRDF4JTest.class.getResource(dbMetadataFile).getPath());

        OntopSQLOWLAPIConfiguration config = builder.build();

        OntopVirtualRepository repo = OntopRepository.defaultRepository(config);
        repo.init();
        /*
         * Prepare the data connection for querying.
         */
        REPO_CONNECTION = repo.getConnection();
    }


    protected static void initR2RML(String dbScriptRelativePath, String r2rmlRelativePath) throws SQLException, IOException {
        initR2RML(dbScriptRelativePath, r2rmlRelativePath, null, null);
    }

    protected static void initR2RML(String dbScriptRelativePath, String r2rmlRelativePath,
                                    @Nullable String ontologyRelativePath) throws SQLException, IOException {
        initR2RML(dbScriptRelativePath, r2rmlRelativePath, ontologyRelativePath, null);
    }

    protected static void initR2RML(String dbScriptRelativePath, String r2rmlRelativePath,
                                    @Nullable String ontologyRelativePath, @Nullable String propertyFile) throws SQLException, IOException {

        String jdbcUrl = URL_PREFIX + UUID.randomUUID().toString();

        SQL_CONNECTION = DriverManager.getConnection(jdbcUrl, USER, PASSWORD);

        java.sql.Statement st = SQL_CONNECTION.createStatement();

        FileReader reader = new FileReader(AbstractRDF4JTest.class.getResource(dbScriptRelativePath).getPath());
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        SQL_CONNECTION.commit();

        OntopSQLOWLAPIConfiguration.Builder<? extends OntopSQLOWLAPIConfiguration.Builder<?>> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .r2rmlMappingFile(AbstractRDF4JTest.class.getResource(r2rmlRelativePath).getPath())
                .jdbcUrl(jdbcUrl)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode();

        if (ontologyRelativePath != null)
            builder.ontologyFile(AbstractRDF4JTest.class.getResource(ontologyRelativePath).getPath());

        if (propertyFile != null)
            builder.propertyFile(AbstractRDF4JTest.class.getResource(propertyFile).getPath());

        OntopSQLOWLAPIConfiguration config = builder.build();

        OntopVirtualRepository repo = OntopRepository.defaultRepository(config);
        repo.init();
        /*
         * Prepare the data connection for querying.
         */
        REPO_CONNECTION = repo.getConnection();
    }

    protected static void release() throws SQLException {
        REPO_CONNECTION.close();
        SQL_CONNECTION.close();
    }

    protected int runQueryAndCount(String queryString) {
        TupleQuery query = REPO_CONNECTION.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult result = query.evaluate();
        int count = 0;
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            LOGGER.debug(bindingSet + "\n");
            count++;
        }
        result.close();
        return count;
    }

    protected void runQueryAndCompare(String queryString, ImmutableSet<String> expectedVValues) {
        runQueryAndCompare(queryString, expectedVValues, new MapBindingSet());
    }

    protected void runQueryAndCompare(String queryString, ImmutableSet<String> expectedVValues,
                                      BindingSet bindings) {
        ImmutableSet<String> vValues = ImmutableSet.copyOf(runQuery(queryString, bindings));
        assertEquals(expectedVValues, vValues);
    }

    protected void runQueryAndCompare(String queryString, ImmutableList<String> expectedVValues) {
        runQueryAndCompare(queryString, expectedVValues, new MapBindingSet());
    }

    protected void runQueryAndCompare(String queryString, ImmutableList<String> expectedVValues,
                                      BindingSet bindings) {
        ImmutableList<String> vValues = runQuery(queryString, bindings);
        assertEquals(expectedVValues, vValues);
    }

    protected String reformulate(String queryString) {
        return REPO_CONNECTION.reformulate(queryString);
    }

    protected ImmutableList<String> runQuery(String queryString) {
        return runQuery(queryString, new MapBindingSet());
    }

    /**
     * Extracts the values of the variable ?v
     */
    protected ImmutableList<String> runQuery(String queryString, BindingSet bindings) {
        TupleQuery query = REPO_CONNECTION.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        bindings.getBindingNames()
                .forEach(n -> query.setBinding(n, bindings.getValue(n)));

        TupleQueryResult result = query.evaluate();
        ImmutableList.Builder<String> vValueBuilder = ImmutableList.builder();
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Optional.ofNullable(bindingSet.getValue("v"))
                    .map(Value::stringValue)
                    .ifPresent(vValueBuilder::add);

            LOGGER.debug(bindingSet + "\n");
        }
        result.close();

        return vValueBuilder.build();
    }

    protected ImmutableList<ImmutableMap<String, String>> executeQuery(String queryString) {
        TupleQuery query = REPO_CONNECTION.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult result = query.evaluate();
        ImmutableList.Builder<ImmutableMap<String, String>> list = ImmutableList.builder();
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
            for (Binding b : bindingSet) {
                map.put(b.getName(), b.getValue().stringValue());
            }
            list.add(map.build());
            LOGGER.debug(bindingSet + "\n");
        }
        result.close();

        return list.build();
    }

    protected void runGraphQueryAndCompare(String queryString, ImmutableSet<Statement> expectedGraph) {
        runGraphQueryAndCompare(queryString, expectedGraph, new MapBindingSet());
    }

    protected void runGraphQueryAndCompare(String queryString, ImmutableSet<Statement> expectedGraph,
                                           BindingSet bindings) {
        GraphQuery query = REPO_CONNECTION.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
        bindings.getBindingNames()
                .forEach(n -> query.setBinding(n, bindings.getValue(n)));

        GraphQueryResult result = query.evaluate();
        ImmutableSet.Builder<org.eclipse.rdf4j.model.Statement> statementBuilder = ImmutableSet.builder();
        while (result.hasNext()) {
            statementBuilder.add(result.next());
        }
        result.close();

        assertEquals(expectedGraph, statementBuilder.build());
    }

    protected TupleQueryResult evaluate(String queryString) {
        TupleQuery query = REPO_CONNECTION.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        return query.evaluate();
    }

    protected GraphQueryResult evaluateGraph(String queryString) {
        GraphQuery query = REPO_CONNECTION.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
        return query.evaluate();
    }
}
