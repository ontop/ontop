-- PostgreSQL SPARQL Federation Functions
-- This file provides PostgreSQL functions for executing SPARQL queries against remote endpoints
-- Requires the pg_http extension or similar HTTP client extension

-- Enable required extensions (uncomment as needed based on your PostgreSQL setup)
-- CREATE EXTENSION IF NOT EXISTS pg_http;
-- CREATE EXTENSION IF NOT EXISTS plperlu;

-- Function to execute a SPARQL SELECT query against a remote endpoint and return results as JSON
-- Parameters:
--   endpoint_url - The SPARQL endpoint URL (e.g., 'https://sparql.uniprot.org/sparql')
--   query_string - The SPARQL query string
-- Returns: JSON text with the query results
CREATE OR REPLACE FUNCTION execute_sparql_query(endpoint_url text, query_string text)
RETURNS text
LANGUAGE plpgsql
AS $$
DECLARE
    result text;
    request_url text;
    response_status integer;
    response_headers text[];
    response_body text;
BEGIN
    -- URL encode the query string
    request_url := endpoint_url || '?query=' || encode(query_string, 'UTF-8') || '&format=json';
    
    -- Make the HTTP GET request
    -- Note: This uses the pg_http extension. If not available, consider using:
    -- - plpython3u with requests library
    -- - plperlu with LWP::UserAgent
    -- - A custom extension
    
    -- Using pg_http extension (if available)
    SELECT http_get(request_url, response_headers, response_body, response_status);
    
    -- Check if the request was successful
    IF response_status >= 200 AND response_status < 300 THEN
        result := response_body;
    ELSE
        RAISE EXCEPTION 'SPARQL query failed with status %: %', response_status, response_body;
    END IF;
    
    RETURN result;
EXCEPTION WHEN undefined_function THEN
    -- Fallback: pg_http not available, try alternative methods
    RAISE EXCEPTION 'pg_http extension not available. Please install it or use an alternative HTTP client.';
END;
$$;

-- Function to execute a SPARQL query and return results as a set of rows
-- This is more complex and requires knowing the result structure
-- For simplicity, we return JSON which can be parsed by the application
CREATE OR REPLACE FUNCTION execute_sparql_query_json(endpoint_url text, query_string text)
RETURNS json
LANGUAGE plpgsql
AS $$
DECLARE
    result_json json;
    query_result text;
BEGIN
    query_result := execute_sparql_query(endpoint_url, query_string);
    
    -- Parse the JSON response
    -- Note: This assumes the SPARQL endpoint returns JSON in the standard format
    SELECT query_result::json INTO result_json;
    
    RETURN result_json;
EXCEPTION WHEN others THEN
    RAISE EXCEPTION 'Failed to execute SPARQL query: %', SQLERRM;
END;
$$;

-- Function to execute a SPARQL CONSTRUCT query and return RDF/XML
CREATE OR REPLACE FUNCTION execute_sparql_construct(endpoint_url text, query_string text)
RETURNS text
LANGUAGE plpgsql
AS $$
DECLARE
    result text;
    request_url text;
    response_status integer;
    response_headers text[];
    response_body text;
BEGIN
    -- URL encode the query string with XML format
    request_url := endpoint_url || '?query=' || encode(query_string, 'UTF-8') || '&format=xml';
    
    -- Make the HTTP GET request
    SELECT http_get(request_url, response_headers, response_body, response_status);
    
    -- Check if the request was successful
    IF response_status >= 200 AND response_status < 300 THEN
        result := response_body;
    ELSE
        RAISE EXCEPTION 'SPARQL CONSTRUCT query failed with status %: %', response_status, response_body;
    END IF;
    
    RETURN result;
END;
$$;

-- Helper function to URL encode strings
CREATE OR REPLACE FUNCTION encode(input text, encoding text)
RETURNS text
LANGUAGE plpgsql
AS $$
DECLARE
    result text;
    i integer;
    c char;
    hex_val text;
BEGIN
    result := '';
    FOR i IN 1..length(input) LOOP
        c := substring(input, i, 1);
        
        -- Check if character needs encoding
        IF c ~ '[A-Za-z0-9_.~-]' THEN
            result := result || c;
        ELSE
            -- Convert to hex
            hex_val := format('%%%02X', ascii(c));
            result := result || hex_val;
        END IF;
    END LOOP;
    
    RETURN result;
END;
$$;

-- Alternative implementation using PL/Python if available
-- This requires the plpython3u extension
CREATE OR REPLACE FUNCTION execute_sparql_query_python(endpoint_url text, query_string text)
RETURNS text
LANGUAGE plpython3u
AS $$
    import urllib.parse
    import urllib.request
    import json
    
    # URL encode the query
    encoded_query = urllib.parse.quote(query_string)
    url = f"{endpoint_url}?query={encoded_query}&format=json"
    
    # Make the request
    try:
        with urllib.request.urlopen(url) as response:
            return response.read().decode('utf-8')
    except Exception as e:
        raise Exception(f"SPARQL query failed: {str(e)}")
$$;

-- Comment: To use these functions, you may need to:
-- 1. Install the pg_http extension: CREATE EXTENSION pg_http;
-- 2. Or install plpython3u: CREATE EXTENSION plpython3u;
-- 3. Ensure your PostgreSQL user has permissions to make HTTP requests
--
-- Example usage:
-- SELECT execute_sparql_query('https://sparql.uniprot.org/sparql', 
--   'SELECT ?protein WHERE { ?protein a <http://purl.uniprot.org/core/Protein> . FILTER regex(?protein, "A4_HUMAN") }');
