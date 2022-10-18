package it.unibz.inf.ontop.docker.lightweight;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.RDFLiteralConstant;
import it.unibz.inf.ontop.model.type.impl.LangDatatype;
import it.unibz.inf.ontop.rdf4j.query.impl.OntopRDF4JBindingSet;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepositoryConnection;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class AbstractDockerRDF4JTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDockerRDF4JTest.class);
    private static OntopRepositoryConnection REPO_CONNECTION;

    protected static void initOBDA(String obdaRelativePath, @Nullable String ontologyRelativePath,
                                   String propertyFile) throws SQLException, IOException {
        initOBDA(obdaRelativePath, ontologyRelativePath, propertyFile, null);
    }

    protected static void initOBDA(String obdaRelativePath, @Nullable String ontologyRelativePath,
                                   String propertyFile, @Nullable String viewFile) throws SQLException, IOException {
        initOBDA(obdaRelativePath, ontologyRelativePath, propertyFile, viewFile, null);
    }

    protected static void initOBDA(String obdaRelativePath, @Nullable String ontologyRelativePath,
                                   String propertyFile, @Nullable String viewFile,
                                   @Nullable String dbMetadataFile) throws SQLException, IOException {

        String propertyFilePath = AbstractDockerRDF4JTest.class.getResource(propertyFile).getPath();

        // The properties required
        String jdbcUrl = null;
        String username = null;
        String password = null;
        String jdbcDriver = null;

        try (InputStream input = Files.newInputStream(Paths.get(propertyFilePath))) {
            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property values
            jdbcUrl = prop.getProperty("jdbc.url");
            username = prop.getProperty("jdbc.user");
            password = prop.getProperty("jdbc.password");
            jdbcDriver = prop.getProperty("jdbc.driver");

        } catch (IOException e) {
            System.out.println("- ERROR loading '" + propertyFile + "'");
        }

        OntopSQLOWLAPIConfiguration.Builder<? extends OntopSQLOWLAPIConfiguration.Builder> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(AbstractDockerRDF4JTest.class.getResource(obdaRelativePath).getPath())
                .jdbcUrl(jdbcUrl)
                .jdbcUser(username)
                .jdbcPassword(password)
                .jdbcDriver(jdbcDriver)
                .enableTestMode();

        if (ontologyRelativePath != null)
            builder.ontologyFile(AbstractDockerRDF4JTest.class.getResource(ontologyRelativePath).getPath());

        builder.propertyFile(AbstractDockerRDF4JTest.class.getResource(propertyFile).getPath());

        if (viewFile != null)
            builder.ontopViewFile(AbstractDockerRDF4JTest.class.getResource(viewFile).getPath());

        if (dbMetadataFile != null)
            builder.dbMetadataFile(AbstractDockerRDF4JTest.class.getResource(dbMetadataFile).getPath());

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

            // CASE 1: RDFLiteralConstant
            Optional.ofNullable(((OntopRDF4JBindingSet) bindingSet).getOntopBinding("v"))
                    .map(OntopBinding::getValue)
                    .filter(v -> v instanceof RDFLiteralConstant)
                    .map(v -> (RDFLiteralConstant) v)
                    .map(v -> v.getType() instanceof LangDatatype
                            ? '"' +  v.getValue() + '"' + v.getType().toString()
                            : '"' +  v.getValue() + '"' + "^^" + v.getType().toString())
                    .ifPresent(vValueBuilder::add);

            // CASE 2: IRIConstant
            Optional.ofNullable(((OntopRDF4JBindingSet) bindingSet).getOntopBinding("v"))
                    .map(OntopBinding::getValue)
                    .filter(v -> v instanceof IRIConstant)
                    .map(Object::toString)
                    .ifPresent(vValueBuilder::add);

            LOGGER.debug(bindingSet + "\n");
        }

        /*while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Optional.ofNullable(bindingSet.getValue("v"))
                    .map(Value::stringValue)
                    .ifPresent(vValueBuilder::add);

            LOGGER.debug(bindingSet + "\n");
        }*/
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

    protected ImmutableList<ImmutableMap<String, String>> executeQueryWithDatatypes(String queryString) {
        TupleQuery query = REPO_CONNECTION.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult result = query.evaluate();
        ImmutableList.Builder<ImmutableMap<String, String>> list = ImmutableList.builder();
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
            OntopRDF4JBindingSet ontopRDF4JBindingSet = (OntopRDF4JBindingSet) bindingSet;
            OntopBinding[] ontopBindings = ontopRDF4JBindingSet.getAllOntopBindings().getBindings();
            Arrays.stream(ontopBindings)
                    .forEach(v -> map.put(v.getName(), v.getValue().toString()));
            list.add(map.build());
            LOGGER.debug(bindingSet + "\n");
        }
        result.close();

        return list.build();
    }
}
