# Ontop-endpoint

## Introduction

The image `ontop/ontop-endpoint` is the standard solution for setting up Ontop SPARQL endpoint. 
One can either use `ontop/ontop-endpoint` directly, or create a dedicated image based on this image.

## Environment variables
Here is a list of environment variables that directly correspond to arguments of the CLI `ontop-endpoint`command. Please refer to [its documentation page for more details about these arguments](https://ontop-vkg.org/guide/cli.html#ontop-endpoint).

- `ONTOP_MAPPING_FILE` (required). Corresponds to the argument `--mapping`.
-  `ONTOP_PROPERTIES_FILE` (required). Corresponds to the argument `--properties`.
-   `ONTOP_ONTOLOGY_FILE` (optional). Corresponds to the argument `--ontology`.
- `ONTOP_XML_CATALOG_FILE` (optional). Corresponds to the argument `--xml-catalog`.
- `ONTOP_CONSTRAINT_FILE` (optional). Corresponds to the argument `--constraint`.
- `ONTOP_CORS_ALLOWED_ORIGINS` (optional). Corresponds to the argument `--cors-allowed-origins`.
- `ONTOP_PORTAL_FILE` (optional). Corresponds to the argument `--portal`.
- `ONTOP_DEV_MODE` (optional). Corresponds to the argument `--dev`.
-  `ONTOP_LAZY_INIT` (optional). Corresponds to the argument `--lazy`.


## Tutorial

A complete tutorial is provided on the Ontop Website: https://ontop-vkg.org/tutorial/endpoint/endpoint-docker.html


