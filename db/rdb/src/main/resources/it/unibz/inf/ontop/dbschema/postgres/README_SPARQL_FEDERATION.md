# PostgreSQL SPARQL Federation Functions

This directory contains PostgreSQL functions for executing SPARQL queries against remote endpoints, enabling SPARQL federation from PostgreSQL.

## Overview

These functions allow you to execute SPARQL queries against remote SPARQL endpoints directly from PostgreSQL/psql. This is useful for:

- Integrating SPARQL data with relational data in PostgreSQL
- Implementing SPARQL federation in ONTOP-based applications
- Querying remote RDF datasets from SQL

## Available Implementations

### 1. PL/pgSQL Implementation (Recommended)

The file `sparql_federation_simple.sql` provides PostgreSQL functions that use the standard `http` extension. This is the most portable solution.

**Requirements:**
- PostgreSQL 9.5+
- `http` extension (included in standard PostgreSQL distributions)

**Installation:**

```sql
-- Install the http extension (requires superuser)
CREATE EXTENSION IF NOT EXISTS http;

-- Configure allowed hosts (optional, for security)
ALTER SYSTEM SET http.allowed_hosts = 'sparql.uniprot.org,dbpedia.org,other-sparql-endpoints';

-- Load the SPARQL functions
\i path/to/sparql_federation_simple.sql
```

**Usage Examples:**

```sql
-- Execute a SPARQL SELECT query and get JSON results
SELECT sparql_query_json('https://sparql.uniprot.org/sparql', 
  'SELECT ?protein WHERE { 
    ?protein a <http://purl.uniprot.org/core/Protein> . 
    FILTER regex(str(?protein), "A4_HUMAN") 
  }');

-- Count results from a SPARQL query
SELECT sparql_count('https://sparql.uniprot.org/sparql', 
  'SELECT ?protein WHERE { ?protein a <http://purl.uniprot.org/core/Protein> }');

-- Execute a SPARQL ASK query
SELECT sparql_ask('https://sparql.uniprot.org/sparql', 
  'ASK { <http://purl.uniprot.org/uniprot/P05067> a <http://purl.uniprot.org/core/Protein> }');
```

### 2. Java Implementation (PL/Java)

The file `SPARQLFederationFunctions.java` provides Java implementations that can be registered as PostgreSQL functions using PL/Java.

**Requirements:**
- PostgreSQL with PL/Java extension installed
- Java 8+

**Installation:**

1. Install PL/Java in your PostgreSQL database
2. Compile the Java class and add it to your classpath
3. Register the functions:

```java
// Java code to register functions
Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/yourdb", "user", "password");
SPARQLFederationFunctions.registerFunctions(conn);
```

Or from SQL (if PL/Java is properly configured):

```sql
-- This requires PL/Java to be installed and the Java class to be in the classpath
CREATE OR REPLACE FUNCTION sparql_query(endpoint_url text, query_text text)
RETURNS text
LANGUAGE java
AS 'it.unibz.inf.ontop.dbschema.impl.postgres.SPARQLFederationFunctions.executeSparqlQuery(java.lang.String, java.lang.String)';
```

## Integration with ONTOP

To use these functions with ONTOP's SPARQL federation support:

### Option 1: Use in OBDA Mappings

You can reference the PostgreSQL functions in your OBDA mappings to execute SPARQL queries against remote endpoints:

```turtle
# Example OBDA mapping that uses SPARQL federation

[mapping1]
 a omm:Mapping;
 omm:sourceQuery """
   SELECT id, sparql_query('https://remote-sparql.org/sparql', 
     'SELECT ?value WHERE { <http://example.org/resource/' || id || '> <http://example.org/property> ?value }') as value
   FROM my_table
 """ ;
 omm:target """
   ?x <http://example.org/property> ?value .
""" .
```

### Option 2: Use in SPARQL Queries with SERVICE

ONTOP already supports SPARQL SERVICE clauses. The existing implementation in `RDF4JInputQueryTranslatorImpl` handles this by making HTTP requests to remote endpoints:

```sparql
PREFIX : <http://example.org/>
SELECT ?x ?value
WHERE {
  ?x :localProperty ?localValue .
  SERVICE <https://remote-sparql.org/sparql> {
    ?x :remoteProperty ?value .
  }
}
```

## Configuration

### Security Considerations

When using the HTTP-based functions:

1. **Allowed Hosts:** Configure which SPARQL endpoints can be accessed:
   ```sql
   ALTER SYSTEM SET http.allowed_hosts = 'trusted-sparql-endpoint.org,another-trusted.org';
   ```

2. **SSL/TLS:** Ensure HTTPS endpoints are properly configured:
   ```sql
   ALTER SYSTEM SET http.ssl_cert_file = '/path/to/cert.pem';
   ALTER SYSTEM SET http.ssl_key_file = '/path/to/key.pem';
   ```

3. **Timeouts:** Configure HTTP request timeouts:
   ```sql
   ALTER SYSTEM SET http.timeout = '5000'; -- 5 seconds
   ```

### Performance Considerations

1. **Caching:** Consider caching frequent SPARQL queries
2. **Batch Processing:** For large result sets, process in batches
3. **Connection Pooling:** Use connection pooling for frequent requests

## Examples

### Example 1: Query UniProt SPARQL Endpoint

```sql
-- Get information about a specific protein
SELECT sparql_query_json('https://sparql.uniprot.org/sparql', 
  'SELECT ?protein ?label WHERE { 
    ?protein a <http://purl.uniprot.org/core/Protein> . 
    ?protein <http://www.w3.org/2000/01/rdf-schema#label> ?label . 
    FILTER regex(str(?protein), "A4_HUMAN") 
  }');
```

### Example 2: Count Proteins from UniProt

```sql
-- Count all proteins
SELECT sparql_count('https://sparql.uniprot.org/sparql', 
  'SELECT ?protein WHERE { ?protein a <http://purl.uniprot.org/core/Protein> }') as protein_count;
```

### Example 3: Check if a Protein Exists

```sql
-- Check if a specific protein exists
SELECT sparql_ask('https://sparql.uniprot.org/sparql', 
  'ASK { <http://purl.uniprot.org/uniprot/P05067> a <http://purl.uniprot.org/core/Protein> }') as exists;
```

### Example 4: Join Local and Remote Data

```sql
-- Join local PostgreSQL data with remote SPARQL data
SELECT local.id, local.name, remote_result
FROM local_table local,
     LATERAL (SELECT sparql_query_json('https://sparql.uniprot.org/sparql', 
       'SELECT ?value WHERE { <http://example.org/' || local.id || '> <http://example.org/property> ?value }') as remote_result) remote
WHERE local.type = 'protein';
```

## Troubleshooting

### Common Issues

1. **http extension not available:**
   - Solution: Install the http extension with superuser privileges
   - `CREATE EXTENSION http;`

2. **Permission denied:**
   - Solution: Grant EXECUTE permissions on the functions
   - `GRANT EXECUTE ON FUNCTION sparql_query(text, text) TO your_user;`

3. **Connection refused or timeout:**
   - Solution: Check if the SPARQL endpoint is accessible
   - Verify firewall settings
   - Increase timeout: `ALTER SYSTEM SET http.timeout = '10000';`

4. **SSL certificate errors:**
   - Solution: Configure SSL certificates or disable verification (not recommended for production)
   - `ALTER SYSTEM SET http.ssl_mode = 'disable';`

## Files in This Directory

- `sparql_federation.sql` - Comprehensive SPARQL functions with multiple implementations
- `sparql_federation_simple.sql` - Simplified version using the http extension
- `SPARQLFederationFunctions.java` - Java implementation for PL/Java
- `README_SPARQL_FEDERATION.md` - This documentation file

## References

- [PostgreSQL http Extension Documentation](https://www.postgresql.org/docs/current/http.html)
- [SPARQL 1.1 Federation Specification](https://www.w3.org/TR/sparql11-federated-query/)
- [RDF4J SPARQL Repository](https://rdf4j.org/documentation/reference/sparql-repository/)
- [ONTOP SPARQL Support](https://ontop-vkg.org/)
