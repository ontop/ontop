package it.unibz.inf.ontop.docker;


import org.eclipse.rdf4j.common.io.IOUtil;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.dawg.DAWGTestResultSetUtil;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultParserRegistry;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.Assert;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class TestExecutor {

    private final String name;
    private final String queryFileURL;
    private final String resultFileURL;
    private final Repository dataRep;
    private final Logger logger;
    private static final String ASK_KEYWORD = "ask";
    private static final String SELECT_KEYWORD = "select";

    public TestExecutor(String name, String queryFileURL, String resultFileURL, Repository dataRep, Logger logger) {
        this.name = name;
        this.queryFileURL = queryFileURL;
		this.resultFileURL = resultFileURL;
        this.dataRep = dataRep;
        this.logger = logger;
    }

    public void runTest() throws Exception {
        try (RepositoryConnection con = dataRep.getConnection()) {
            String queryString = readQueryString();

            // NB: the query may be wrong (on purpose) so we cannot use the RDF4J parser
            if (isSelectQuery(queryString)) {
                executeSelectQuery(con, queryString);
            } else if (isAskQuery(queryString)) {
                executeAskQuery(con, queryString);
            } else {
                throw new IllegalArgumentException("Only ASK and SELECT SPARQL queries are supported\n Query: "
                        + queryString);
            }
        }
    }

    private void executeSelectQuery(RepositoryConnection con, String queryString) throws Exception {
        ResultSetInfo expectedTupleResult = readResultSetInfo();

        try {
            TupleQuery query = (TupleQuery) con.prepareQuery(QueryLanguage.SPARQL, queryString, queryFileURL);
            TupleQueryResult queryResult = query.evaluate();
            compareResultSize(queryResult, expectedTupleResult);

        } catch (Exception e) {
            //e.printStackTrace();
            compareThrownException(e, expectedTupleResult); // compare the thrown exception class
        }
    }

    /**
     * Exceptions are not yet expected for ASK queries
     */
    private void executeAskQuery(RepositoryConnection con, String queryString) throws Exception {
        BooleanQuery query = (BooleanQuery) con.prepareQuery(QueryLanguage.SPARQL, queryString, queryFileURL);
        boolean queryResult = query.evaluate();
        boolean expectedResult = readExpectedBooleanQueryResult();
        if (expectedResult != queryResult)
            throw new Exception("Expected: " + expectedResult + ", result: " + queryResult);
    }

    private void compareResultSize(TupleQueryResult queryResult, ResultSetInfo expectedResult) {
        int queryResultSize = countTuple(queryResult);
        int expectedResultSize = (Integer) attributeValue(expectedResult, "counter");
        if (queryResultSize != expectedResultSize) {
            StringBuilder message = new StringBuilder(128);
            message.append("\n============ ");
            message.append(name);
            message.append(" =======================\n");
            message.append("Expected result: ");
            message.append(expectedResultSize);
            message.append("\n");
            message.append("Query result: ");
            message.append(queryResultSize);
            message.append("\n");
            message.append("=====================================\n");
            //logger.error(message.toString());
            Assert.fail(message.toString());
        }
    }

    private boolean readExpectedBooleanQueryResult()
            throws Exception
    {
        Optional<QueryResultFormat> bqrFormat = BooleanQueryResultParserRegistry.getInstance().getFileFormatForFileName(resultFileURL);

        if (bqrFormat.isPresent()) {
            try (InputStream in = new URL(resultFileURL).openStream()) {
                return QueryResultIO.parseBoolean(in, bqrFormat.get());
            }
        }
        else {
            Set<Statement> resultGraph = readExpectedGraphQueryResult();
            return DAWGTestResultSetUtil.toBooleanQueryResult(resultGraph);
        }
    }

    private Set<Statement> readExpectedGraphQueryResult()
            throws Exception
    {
        Optional<RDFFormat> rdfFormat = Rio.getParserFormatForFileName(resultFileURL);

        if (rdfFormat.isPresent()) {
            RDFParser parser = Rio.createParser(rdfFormat.get(), dataRep.getValueFactory());
            ParserConfig config = parser.getParserConfig();
            // To emulate DatatypeHandling.IGNORE
            config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
            config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
            config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
            config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
//			parser.setDatatypeHandling(DatatypeHandling.IGNORE);
//			parser.setPreserveBNodeIDs(true);

            Set<Statement> result = new LinkedHashSet<Statement>();
            parser.setRDFHandler(new StatementCollector(result));

            try (InputStream in = new URL(resultFileURL).openStream()) {
                parser.parse(in, resultFileURL);
            }

            return result;
        }
        else {
            throw new RuntimeException("Unable to determine file type of results file");
        }
    }

    private void compareThrownException(Exception ex, ResultSetInfo expectedResult) throws Exception {
        String thrownException = ex.getClass().getName();
        String expectedThrownException = (String) attributeValue(expectedResult, "thrownException");
        if (!thrownException.equals(expectedThrownException)) {
            StringBuilder message = new StringBuilder(128);
            message.append("\n============ ");
            message.append(name);
            message.append(" =======================\n");
            message.append("Expected thrown exception: ");
            message.append(expectedThrownException);
            message.append("\n");
            message.append("Thrown exception: ");
            message.append(thrownException);
            message.append("\n");
            message.append("Message:" + ex.getMessage());
            message.append("=====================================\n");
            //logger.error(message.toString());
            throw new Exception(message.toString());
        }
    }

    private int countTuple(TupleQueryResult tuples) throws QueryEvaluationException {
        if (tuples == null) {
            return -1;
        }
        int counter = 0;
        while (tuples.hasNext()) {
            counter++;
            BindingSet bs = tuples.next();
            String msg = String.format("x: %s, y: %s\n", bs.getValue("x"), bs.getValue("y"));
            logger.debug(msg);
        }
        return counter;
    }

    private Object attributeValue(ResultSetInfo rsInfo, String attribute) throws QueryEvaluationException {
        return rsInfo.get(attribute);
    }


    private String readQueryString() throws IOException {
        try (InputStream stream = new URL(queryFileURL).openStream()) {
            return IOUtil.readString(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }

    private ResultSetInfo readResultSetInfo() throws Exception {
        Set<Statement> resultGraph = readGraphResultSetInfo();
        return ResultSetInfo.toResultSetInfo(resultGraph);
    }

    private Set<Statement> readGraphResultSetInfo() throws Exception {
        RDFFormat rdfFormat = Rio.getParserFormatForFileName(resultFileURL).get();
        if (rdfFormat != null) {
            RDFParser parser = Rio.createParser(rdfFormat, dataRep.getValueFactory());
            ParserConfig config = parser.getParserConfig();
            // To emulate DatatypeHandling.IGNORE
            config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
            config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
            config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
            config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);

//			parser.setDatatypeHandling(DatatypeHandling.IGNORE);
//			parser.setPreserveBNodeIDs(true);

            Set<Statement> result = new LinkedHashSet<>();
            parser.setRDFHandler(new StatementCollector(result));

            try (InputStream in = new URL(resultFileURL).openStream()) {
                parser.parse(in, resultFileURL);
            }
            return result;
        } else {
            throw new RuntimeException("Unable to determine file type of results file");
        }
    }

    public static boolean isAskQuery(String query) {
        return query.toLowerCase().contains(ASK_KEYWORD);
    }

    public static boolean isSelectQuery(String query) {
        return query.toLowerCase().contains(SELECT_KEYWORD);
    }
}
