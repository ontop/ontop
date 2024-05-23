package it.unibz.inf.ontop.docker.lightweight;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepositoryConnection;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class AbstractDockerRDF4JTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDockerRDF4JTest.class);
    private static OntopRepositoryConnection REPO_CONNECTION;

    protected static void initOBDA(String obdaRelativePath, @Nullable String ontologyRelativePath,
                                   String propertyFile) {
        initOBDA(obdaRelativePath, ontologyRelativePath, propertyFile, null);
    }

    protected static void initOBDA(String obdaRelativePath, @Nullable String ontologyRelativePath,
                                   String propertyFile, @Nullable String lensesFile) {
        initOBDA(obdaRelativePath, ontologyRelativePath, propertyFile, lensesFile, null);
    }

    protected static void initOBDA(String obdaRelativePath, @Nullable String ontologyRelativePath,
                                   String propertyFile, @Nullable String lensesFile,
                                   @Nullable String dbMetadataFile) {

        String propertyFilePath = AbstractDockerRDF4JTest.class.getResource(propertyFile).getPath();

        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(AbstractDockerRDF4JTest.class.getResource(obdaRelativePath).getPath())
                .propertyFile(propertyFilePath)
                .enableTestMode();

        if (ontologyRelativePath != null)
            builder.ontologyFile(AbstractDockerRDF4JTest.class.getResource(ontologyRelativePath).getPath());

        builder.propertyFile(AbstractDockerRDF4JTest.class.getResource(propertyFile).getPath());

        if (lensesFile != null)
            builder.lensesFile(AbstractDockerRDF4JTest.class.getResource(lensesFile).getPath());

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

    protected static void release() {
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

    protected void executeAndCompareValuesAny(String queryString, ImmutableList<ImmutableSet<String>> expectedVValues) {
        executeAndCompareValuesAny(queryString, expectedVValues, new MapBindingSet());
    }

    protected void executeAndCompareValuesAny(String queryString, ImmutableList<ImmutableSet<String>> expectedVValues,
                                              BindingSet bindings) {
        ImmutableSet<String> vValues = ImmutableSet.copyOf(runQuery(queryString, bindings));
        List<AssertionError> errors = new ArrayList<>();
        for(var possibleResult : expectedVValues) {
            try {
                assertEquals(possibleResult, vValues);
                return;
            }
            catch (AssertionError e) {
                errors.add(e);
            }
        }
        throw errors.get(0);
    }

    protected void executeAndCompareValues(String queryString, ImmutableSet<String> expectedVValues) {
        executeAndCompareValues(queryString, expectedVValues, new MapBindingSet());
    }

    protected void executeAndCompareValues(String queryString, ImmutableSet<String> expectedVValues,
                                           BindingSet bindings) {
        ImmutableSet<String> vValues = ImmutableSet.copyOf(runQuery(queryString, bindings));
        assertEquals(expectedVValues, vValues);
    }

    protected void executeAndCompareValues(String queryString, ImmutableMultiset<String> expectedVValues) {
        executeAndCompareValues(queryString, expectedVValues, new MapBindingSet());
    }

    protected void executeAndCompareValues(String queryString, ImmutableMultiset<String> expectedVValues,
                                           BindingSet bindings) {
        ImmutableMultiset<String> vValues = ImmutableMultiset.copyOf(runQuery(queryString, bindings));
        assertEquals(expectedVValues, vValues);
    }

    protected void executeAndCompareValues(String queryString, ImmutableList<String> expectedVValues) {
        executeAndCompareValues(queryString, expectedVValues, new MapBindingSet());
    }

    protected void executeAndCompareValues(String queryString, ImmutableList<String> expectedVValues,
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
            Value bindingSetValue = bindingSet.getValue("v");
            // CASE 1: SimpleIRI
            if (bindingSetValue instanceof SimpleIRI) {
                vValueBuilder.add("<" + bindingSetValue.stringValue() + ">");
            // CASE 2: SimpleLiteral
            } else if (bindingSetValue instanceof SimpleLiteral) {
                SimpleLiteral simpleLiteral = (SimpleLiteral) bindingSetValue;
                if (simpleLiteral.getLanguage().isPresent()) {
                    // CASE 2.1: Literal with language tag
                    vValueBuilder.add("\"" + simpleLiteral.stringValue() + "\"" + "@" + simpleLiteral.getLanguage().get());
                } else {
                    // CASE 2.2: Literal without a language tag
                    // CASE 2.2.1: rdf PlainLiteral
                    if (((SimpleLiteral) bindingSetValue).getDatatype().stringValue().endsWith("#PlainLiteral")) {
                            vValueBuilder.add("\"" + bindingSetValue.stringValue() + "\"^^xsd:string");
                    } else {
                    // CASE 2.2.2: xsd datatype, or wkt literal
                            vValueBuilder.add("\"" + simpleLiteral.stringValue() + "\"" + "^^" +
                            ((SimpleLiteral) bindingSetValue).getDatatype().stringValue()
                                    .replace("http://www.w3.org/2001/XMLSchema#", "xsd:")
                                    .replace("http://www.opengis.net/ont/geosparql#", "geo:"));
                    }
                }
            }

            LOGGER.debug(bindingSet + "\n");
        }
        result.close();

        return vValueBuilder.build();
    }

    protected ImmutableList<ImmutableMap<String, String>> executeAndCompareBindingValues(String queryString) {
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

    protected ImmutableSet<ImmutableMap<String, String>> executeQueryAndCompareBindingLexicalValues(String queryString) {
        TupleQuery query = REPO_CONNECTION.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult result = query.evaluate();
        ImmutableSet.Builder<ImmutableMap<String, String>> set = ImmutableSet.builder();
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
            for (Binding b : bindingSet) {
                Value bindingSetValue = b.getValue();
                if (bindingSetValue instanceof SimpleIRI) {
                    map.put(b.getName(), "<" + bindingSetValue.stringValue() + ">");
                } else if (bindingSetValue instanceof SimpleLiteral) {
                    SimpleLiteral simpleLiteral = (SimpleLiteral) bindingSetValue;
                    String finalLiteral = simpleLiteral.getLanguage().isPresent()
                            ? "\"" + simpleLiteral.stringValue() + "\"" + "@" + simpleLiteral.getLanguage().get()
                            : "\"" + simpleLiteral.stringValue() + "\"" + "^^" + simpleLiteral.getDatatype().stringValue()
                                    .replace("http://www.w3.org/2001/XMLSchema#", "xsd:");
                    map.put(b.getName(), finalLiteral);
                }
            }
            set.add(map.build());
            LOGGER.debug(bindingSet + "\n");
        }
        result.close();
        
        return set.build();
    }
}
