-- PostgreSQL initialization script for SPARQL federation testing
-- This script is executed when the container starts

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS http;
CREATE EXTENSION IF NOT EXISTS plpgsql;

-- Configure http extension settings (optional, for testing)
ALTER SYSTEM SET http.allowed_hosts = 'sparql.uniprot.org,dbpedia.org,localhost,127.0.0.1';
ALTER SYSTEM SET http.timeout = '10000';

-- Create test schema
CREATE SCHEMA IF NOT EXISTS sparql_test;

-- Load SPARQL federation functions
\echo 'Loading SPARQL federation functions...'

-- Create the functions from the SQL file
-- Note: In Docker, we need to use the absolute path or copy the file
-- For this init script, we'll create the functions directly

CREATE OR REPLACE FUNCTION sparql_query(endpoint_url text, query_text text, DEFAULT format text)
RETURNS text
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    full_url text;
    response text;
    status integer;
    headers text[];
BEGIN
    -- Default format to JSON
    IF format IS NULL THEN
        format := 'json';
    END IF;
    
    -- Construct the full URL with query parameters
    full_url := endpoint_url || '?query=' || 
               replace(replace(query_text, ' ', '+'), '
', '%0A') || 
               '&format=' || format;
    
    -- Try to use the http extension
    BEGIN
        -- This uses the http extension's http_get function
        -- Syntax: http_get(url, headers, body, status)
        SELECT http_get(full_url, ARRAY['Accept: application/sparql-results+' || format], response, status);
        
        IF status >= 200 AND status < 300 THEN
            RETURN response;
        ELSE
            RAISE EXCEPTION 'SPARQL query failed with HTTP status %: %', status, response;
        END IF;
    EXCEPTION WHEN undefined_function THEN
        -- Fallback: http extension not available
        RAISE EXCEPTION 'http extension not available. Please install it with: CREATE EXTENSION http;';
    END;
END;
$$;

CREATE OR REPLACE FUNCTION sparql_query_json(endpoint_url text, query_text text)
RETURNS json
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN sparql_query(endpoint_url, query_text, 'json')::json;
END;
$$;

CREATE OR REPLACE FUNCTION sparql_construct(endpoint_url text, query_text text)
RETURNS text
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN sparql_query(endpoint_url, query_text, 'xml');
END;
$$;

CREATE OR REPLACE FUNCTION sparql_ask(endpoint_url text, query_text text)
RETURNS boolean
LANGUAGE plpgsql
AS $$
DECLARE
    result_json json;
    ask_result boolean;
BEGIN
    result_json := sparql_query_json(endpoint_url, query_text);
    
    -- Parse the JSON response to get the boolean result
    -- Standard SPARQL JSON format: {"head": {...}, "boolean": true/false}
    ask_result := result_json->'boolean';
    
    IF ask_result IS NULL THEN
        RAISE EXCEPTION 'Invalid SPARQL ASK response format';
    END IF;
    
    RETURN ask_result;
END;
$$;

CREATE OR REPLACE FUNCTION sparql_count(endpoint_url text, query_text text)
RETURNS bigint
LANGUAGE plpgsql
AS $$
DECLARE
    result_json json;
    bindings_array json[];
BEGIN
    result_json := sparql_query_json(endpoint_url, query_text);
    
    -- Parse the JSON response to count bindings
    -- Standard SPARQL JSON format: {"head": {...}, "results": {"bindings": [...]}}
    bindings_array := result_json->'results'->'bindings';
    
    IF bindings_array IS NULL THEN
        RETURN 0;
    END IF;
    
    RETURN jsonb_array_length(bindings_array::jsonb);
END;
$$;

-- Create test tables for SPARQL federation testing
CREATE TABLE IF NOT EXISTS sparql_test.local_data (
    id serial PRIMARY KEY,
    name text,
    uri text
);

-- Insert test data
INSERT INTO sparql_test.local_data (name, uri) VALUES 
    ('Test Protein 1', 'http://purl.uniprot.org/uniprot/P05067'),
    ('Test Protein 2', 'http://purl.uniprot.org/uniprot/Q99999'),
    ('Test Protein 3', 'http://purl.uniprot.org/uniprot/A4_HUMAN');

-- Grant permissions to test user
GRANT USAGE ON SCHEMA sparql_test TO testuser;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA sparql_test TO testuser;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO testuser;

-- Create a view that combines local and remote data
CREATE OR REPLACE VIEW sparql_test.combined_view AS
SELECT 
    ld.id,
    ld.name,
    ld.uri,
    sparql_query_json('https://sparql.uniprot.org/sparql', 
      'SELECT ?label WHERE { <' || ld.uri || '> <http://www.w3.org/2000/01/rdf-schema#label> ?label }') as remote_label
FROM sparql_test.local_data ld;

\echo 'SPARQL federation functions loaded successfully!'
