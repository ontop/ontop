# Ontop DuckDB example image

This folder contains a self-contained example that packages Ontop together with a DuckDB database and RDF 1.2/RDF-star mappings.

## Build

```bash
# from the repository root
docker build -f client/docker/duckdb/Dockerfile -t ontop-duckdb client/docker/duckdb
```

## Run

```bash
docker run --rm -p 8080:8080 ontop-duckdb
```

Once the container is up, open http://localhost:8080/ to access the Ontop portal and issue SPARQL queries against the DuckDB dataset. The RDF-star annotations are published through the mapping stored in `mapping-rdfstar.obda`.
