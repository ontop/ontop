-- PostgreSQL SPARQL Federation Functions (Simple Version)
-- Uses the standard 'http' extension which is more commonly available

-- Check if http extension exists, otherwise create a stub
DO $$
BEGIN
    -- Try to create the http extension if it doesn't exist
    -- Note: This requires superuser privileges
    -- In production, you would run: CREATE EXTENSION http;
    
    -- Check if we can use http extension
    PERFORM 1 FROM pg_extension WHERE extname = 'http';
    IF NOT FOUND THEN
        RAISE NOTICE 'http extension not found. SPARQL functions will use fallback implementation.';
    END IF;
END
$$;

-- Main function to execute SPARQL queries against remote endpoints
-- Uses the http extension if available, otherwise provides a fallback
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

-- Function to execute SPARQL query and return as JSON
CREATE OR REPLACE FUNCTION sparql_query_json(endpoint_url text, query_text text)
RETURNS json
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN sparql_query(endpoint_url, query_text, 'json')::json;
END;
$$;

-- Function to execute SPARQL CONSTRUCT query
CREATE OR REPLACE FUNCTION sparql_construct(endpoint_url text, query_text text)
RETURNS text
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN sparql_query(endpoint_url, query_text, 'xml');
END;
$$;

-- Function to execute SPARQL ASK query (returns boolean)
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

-- Function to count results from a SPARQL SELECT query
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

-- Comment: Installation instructions
-- ================================
-- 
-- 1. Install the http extension (requires superuser):
--    CREATE EXTENSION http;
--
-- 2. Grant permissions to users who need to execute SPARQL queries:
--    GRANT USAGE ON SCHEMA public TO your_user;
--    GRANT EXECUTE ON FUNCTION sparql_query(text, text, text) TO your_user;
--    GRANT EXECUTE ON FUNCTION sparql_query_json(text, text) TO your_user;
--    GRANT EXECUTE ON FUNCTION sparql_construct(text, text) TO your_user;
--    GRANT EXECUTE ON FUNCTION sparql_ask(text, text) TO your_user;
--    GRANT EXECUTE ON FUNCTION sparql_count(text, text) TO your_user;
--
-- 3. Example usage from psql:
--    
--    -- Simple SELECT query
--    SELECT sparql_query_json('https://sparql.uniprot.org/sparql', 
--      'SELECT ?protein WHERE { ?protein a <http://purl.uniprot.org/core/Protein> . 
--        FILTER regex(str(?protein), "A4_HUMAN") }');
--    
--    -- Count results
--    SELECT sparql_count('https://sparql.uniprot.org/sparql', 
--      'SELECT ?protein WHERE { ?protein a <http://purl.uniprot.org/core/Protein> }');
--    
--    -- ASK query
--    SELECT sparql_ask('https://sparql.uniprot.org/sparql', 
--      'ASK { <http://purl.uniprot.org/uniprot/P05067> a <http://purl.uniprot.org/core/Protein> }');
--
-- Note: The http extension has security restrictions. You may need to:
-- - Add the SPARQL endpoint to the http.allowed_hosts configuration
-- - Configure http.allowed_schemas (usually just 'http' and 'https')
-- - Set http.allowed_redirects if needed
