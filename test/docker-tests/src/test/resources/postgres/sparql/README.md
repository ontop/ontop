# PostgreSQL SPARQL Federation Docker Test Setup

This directory contains Docker configuration for testing the PostgreSQL SPARQL federation functions.

## Quick Start

### Using Docker Compose

1. **Start the PostgreSQL container:**
   ```bash
   cd test/docker-tests/src/test/resources/postgres/sparql
   docker-compose up -d
   ```

2. **Connect to PostgreSQL:**
   ```bash
   psql -h localhost -p 5432 -U testuser -d ontop_test
   ```
   Password: `testpass`

3. **Test the SPARQL functions:**
   ```sql
   -- Test a simple SPARQL query
   SELECT sparql_query_json('https://sparql.uniprot.org/sparql', 
     'SELECT ?protein WHERE { 
       ?protein a <http://purl.uniprot.org/core/Protein> . 
       FILTER regex(str(?protein), "A4_HUMAN") 
     }');
   
   -- Count results
   SELECT sparql_count('https://sparql.uniprot.org/sparql', 
     'SELECT ?protein WHERE { ?protein a <http://purl.uniprot.org/core/Protein> }');
   
   -- Test ASK query
   SELECT sparql_ask('https://sparql.uniprot.org/sparql', 
     'ASK { <http://purl.uniprot.org/uniprot/P05067> a <http://purl.uniprot.org/core/Protein> }');
   ```

4. **View the combined data:**
   ```sql
   SELECT * FROM sparql_test.combined_view;
   ```

5. **Stop the container:**
   ```bash
   docker-compose down
   ```

## Running the Docker Tests

The test class `SPARQLFederationDockerTest.java` uses Testcontainers to automatically spin up a PostgreSQL instance for testing.

### Prerequisites

- Java 8+
- Maven
- Docker (running and accessible to the current user)

### Running the tests

```bash
cd /path/to/ontop
mvn test -Dtest=SPARQLFederationDockerTest -pl test/docker-tests
```

Or run all PostgreSQL-related tests:
```bash
mvn test -Dtest=*Postgres*Test -pl test/docker-tests
```

## Configuration

### Environment Variables

You can configure the PostgreSQL container using environment variables:

```bash
# Custom database name
export POSTGRES_DB=my_test_db

# Custom credentials
export POSTGRES_USER=myuser
export POSTGRES_PASSWORD=mypassword

# Start the container
docker-compose up -d
```

### Custom Docker Images

To use a different PostgreSQL version, modify the `docker-compose.yml` file:

```yaml
services:
  postgres-sparql:
    image: postgres:14-alpine  # Change to your desired version
    # ...
```

### Adding a Local SPARQL Endpoint

For testing with a local SPARQL endpoint, uncomment the Fuseki service in `docker-compose.yml`:

```yaml
services:
  # ... existing services ...
  
  fuseki:
    image: stain/jena-fuseki:4.3.2
    container_name: ontop-fuseki
    environment:
      ADMIN_PASSWORD: admin
    ports:
      - "3030:3030"
    volumes:
      - ./fuseki-data:/fuseki
```

Then start the services:
```bash
docker-compose up -d
```

You can then test with the local endpoint:
```sql
SELECT sparql_query_json('http://fuseki:3030/my-dataset/sparql', 
  'SELECT ?x WHERE { ?x ?p ?o } LIMIT 10');
```

## Troubleshooting

### Docker Compose Issues

**Error: "No such file or directory"**
- Make sure you're in the correct directory: `test/docker-tests/src/test/resources/postgres/sparql/`

**Error: "Port already in use"**
- Stop the existing container: `docker-compose down`
- Or use a different port in `docker-compose.yml`

### PostgreSQL Connection Issues

**Error: "Connection refused"**
- Check if PostgreSQL is running: `docker ps`
- Check the logs: `docker logs ontop-postgres-sparql`
- Wait a few seconds for PostgreSQL to start

**Error: "Password authentication failed"**
- Verify the credentials in `docker-compose.yml`
- Default: user=`testuser`, password=`testpass`, database=`ontop_test`

### HTTP Extension Issues

**Error: "http extension not available"**
- The http extension should be installed automatically via `init-sparql.sql`
- Check if it's installed: `SELECT * FROM pg_extension WHERE extname = 'http';`
- If not, install manually: `CREATE EXTENSION http;` (requires superuser)

**Error: "Permission denied for http extension"**
- The http extension requires superuser privileges to install
- In the Docker container, the default user has superuser privileges
- If testing locally, you may need to run as postgres user

### SPARQL Endpoint Issues

**Error: "Connection refused" or timeout**
- The SPARQL endpoint might be blocking requests
- Check if the endpoint is accessible from your machine
- Try with a different endpoint (e.g., `https://dbpedia.org/sparql`)

**Error: "SSL certificate"**
- The http extension may reject self-signed certificates
- Configure SSL settings: `ALTER SYSTEM SET http.ssl_mode = 'disable';` (not recommended for production)

## Files in This Directory

- `docker-compose.yml` - Docker Compose configuration
- `init-sparql.sql` - PostgreSQL initialization script
- `README.md` - This documentation file

## References

- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)
- [Testcontainers](https://www.testcontainers.org/)
- [PostgreSQL http Extension](https://www.postgresql.org/docs/current/http.html)
- [SPARQL 1.1 Federation](https://www.w3.org/TR/sparql11-federated-query/)
