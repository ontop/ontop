package it.unibz.inf.ontop.dbschema.impl.postgres;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for SPARQL federation functions that can be called from PostgreSQL.
 * This class provides methods that can be exposed as PostgreSQL functions through JDBC.
 */
public class SPARQLFederationFunctions {

    /**
     * Executes a SPARQL SELECT query against a remote endpoint and returns the results as JSON.
     * 
     * @param endpointUrl The SPARQL endpoint URL
     * @param query The SPARQL query string
     * @return JSON string with query results
     * @throws SQLException if the query fails
     */
    public static String executeSparqlQuery(String endpointUrl, String query) throws SQLException {
        Repository repo = null;
        RepositoryConnection conn = null;
        
        try {
            repo = new SPARQLRepository(endpointUrl);
            repo.init();
            conn = repo.getConnection();
            
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            
            // Execute and collect results
            List<String> results = new ArrayList<>();
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("[{");
                boolean first = true;
                
                while (result.hasNext()) {
                    if (!first) {
                        jsonBuilder.append(",");
                    }
                    first = false;
                    
                    BindingSet bindingSet = result.next();
                    jsonBuilder.append("{");
                    boolean firstBinding = true;
                    
                    for (String bindingName : bindingSet.getBindingNames()) {
                        if (!firstBinding) {
                            jsonBuilder.append(",");
                        }
                        firstBinding = false;
                        
                        String value = bindingSet.getValue(bindingName).stringValue();
                        jsonBuilder.append("\"").append(bindingName).append("\":\"")
                                .append(escapeJson(value)).append("\"");
                    }
                    
                    jsonBuilder.append("}");
                }
                
                jsonBuilder.append("}]");
                return jsonBuilder.toString();
            }
            
        } catch (Exception e) {
            throw new SQLException("SPARQL query execution failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
            if (repo != null) {
                try {
                    repo.shutDown();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Executes a SPARQL query and returns the count of results.
     * 
     * @param endpointUrl The SPARQL endpoint URL
     * @param query The SPARQL query string
     * @return The number of results
     * @throws SQLException if the query fails
     */
    public static long executeSparqlCount(String endpointUrl, String query) throws SQLException {
        Repository repo = null;
        RepositoryConnection conn = null;
        
        try {
            repo = new SPARQLRepository(endpointUrl);
            repo.init();
            conn = repo.getConnection();
            
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                long count = 0;
                while (result.hasNext()) {
                    result.next();
                    count++;
                }
                return count;
            }
            
        } catch (Exception e) {
            throw new SQLException("SPARQL count query failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
            if (repo != null) {
                try {
                    repo.shutDown();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Executes a SPARQL ASK query and returns a boolean result.
     * 
     * @param endpointUrl The SPARQL endpoint URL
     * @param query The SPARQL query string
     * @return true if the ASK query returns true, false otherwise
     * @throws SQLException if the query fails
     */
    public static boolean executeSparqlAsk(String endpointUrl, String query) throws SQLException {
        Repository repo = null;
        RepositoryConnection conn = null;
        
        try {
            repo = new SPARQLRepository(endpointUrl);
            repo.init();
            conn = repo.getConnection();
            
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                return result.hasNext();
            }
            
        } catch (Exception e) {
            throw new SQLException("SPARQL ASK query failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
            if (repo != null) {
                try {
                    repo.shutDown();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Escapes special characters for JSON string values.
     */
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * Registers the SPARQL federation functions in a PostgreSQL database.
     * This method should be called after establishing a JDBC connection to PostgreSQL.
     * 
     * @param connection The JDBC connection to PostgreSQL
     * @throws SQLException if function registration fails
     */
    public static void registerFunctions(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create the function for executing SPARQL queries
            stmt.execute("CREATE OR REPLACE FUNCTION sparql_query(endpoint_url text, query_text text) " +
                         "RETURNS text " +
                         "LANGUAGE java " +
                         "AS 'it.unibz.inf.ontop.dbschema.impl.postgres.SPARQLFederationFunctions.executeSparqlQuery(java.lang.String, java.lang.String)'");
            
            // Create the function for counting SPARQL results
            stmt.execute("CREATE OR REPLACE FUNCTION sparql_count(endpoint_url text, query_text text) " +
                         "RETURNS bigint " +
                         "LANGUAGE java " +
                         "AS 'it.unibz.inf.ontop.dbschema.impl.postgres.SPARQLFederationFunctions.executeSparqlCount(java.lang.String, java.lang.String)'");
            
            // Create the function for SPARQL ASK queries
            stmt.execute("CREATE OR REPLACE FUNCTION sparql_ask(endpoint_url text, query_text text) " +
                         "RETURNS boolean " +
                         "LANGUAGE java " +
                         "AS 'it.unibz.inf.ontop.dbschema.impl.postgres.SPARQLFederationFunctions.executeSparqlAsk(java.lang.String, java.lang.String)'");
            
        } catch (SQLException e) {
            // If LANGUAGE java is not available, try alternative approaches
            throw new SQLException("Failed to register SPARQL federation functions. " +
                                  "PL/Java may not be installed. Error: " + e.getMessage(), e);
        }
    }

    /**
     * Unregisters the SPARQL federation functions from a PostgreSQL database.
     * 
     * @param connection The JDBC connection to PostgreSQL
     * @throws SQLException if function unregistration fails
     */
    public static void unregisterFunctions(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP FUNCTION IF EXISTS sparql_query(text, text)");
            stmt.execute("DROP FUNCTION IF EXISTS sparql_count(text, text)");
            stmt.execute("DROP FUNCTION IF EXISTS sparql_ask(text, text)");
        }
    }
}
