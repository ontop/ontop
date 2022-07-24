# Ontop-endpoint

## Introduction

The image `ontop/ontop-endpoint` is the standard solution for setting up Ontop SPARQL endpoint. 
One can either use `ontop/ontop-endpoint` directly, or create a dedicated image based on this image.

## Environment variables
Here is a list of environment variables. Most of them directly correspond to arguments of the CLI `ontop-endpoint`command. Please refer to [its documentation page for more details about these arguments](https://ontop-vkg.org/guide/cli.html#ontop-endpoint).

- `ONTOP_MAPPING_FILE` (required). Corresponds to the argument `--mapping`.
- `ONTOP_PROPERTIES_FILE` (optional). Corresponds to the argument `--properties`.
- `ONTOP_ONTOLOGY_FILE` (optional). Corresponds to the argument `--ontology`.
- `ONTOP_DB_PASSWORD` (optional). Corresponds to the argument `--db-password`. Added in 4.1.0.
- `ONTOP_DB_PASSWORD_FILE` (optional). Loads the DB password from a separate file (e.g. a Docker secret) and assigns it to the argument `--db-password`. Introduced in 4.1.0.
- `ONTOP_DB_USER` (optional). Corresponds to the argument `--db-user`. Added in 4.1.0.
- `ONTOP_DB_USER_FILE` (optional). Loads the DB user from a separate file (e.g. a Docker secret) and assigns it to the argument `--db-user`. Introduced in 4.1.0.
- `ONTOP_DB_URL` (optional). Corresponds to the argument `--db-url`. Added in 4.1.0.
- `ONTOP_DB_URL_FILE` (optional). Loads the DB url from a separate file (e.g. a Docker secret) and assigns it to the argument `--db-url`. Introduced in 4.1.0.
- `ONTOP_DB_DRIVER` (optional). Corresponds to the argument `--db-driver`. Added in 4.2.0.
- `ONTOP_XML_CATALOG_FILE` (optional). Corresponds to the argument `--xml-catalog`.
- `ONTOP_CONSTRAINT_FILE` (optional). Corresponds to the argument `--constraint`.
- `ONTOP_DB_METADATA_FILE` (optional). Corresponds to the argument `--db-metadata`. Added in 4.1.0.
- `ONTOP_VIEW_FILE` (optional). Corresponds to the argument `--ontop-views`. Added in 4.1.0.
- `ONTOP_CORS_ALLOWED_ORIGINS` (optional). Corresponds to the argument `--cors-allowed-origins`.
- `ONTOP_PORTAL_FILE` (optional). Corresponds to the argument `--portal`.
- `ONTOP_DISABLE_PORTAL_PAGE` (optional). Corresponds to the argument `--disable-portal-page`. Added in 4.2.0.
- `ONTOP_DEV_MODE` (optional). Corresponds to the argument `--dev`.
- `ONTOP_LAZY_INIT` (optional). Corresponds to the argument `--lazy`.
- `ONTOP_PREDEFINED_CONFIG` (optional). Corresponds to the argument `--predefined-config`.
- `ONTOP_PREDEFINED_QUERIES` (optional). Corresponds to the argument `--predefined-queries`.
- `ONTOP_CONTEXTS` (optional). Corresponds to the argument `--contexts`.
- `ONTOP_DEBUG` (optional). Uses debug for logback.
- `ONTOP_JAVA_ARGS` (optional). Allows to set arbitrary Java arguments. Added in 4.1.0.
- `ONTOP_FILE_ENCODING` (optional). File encoding. By default, set to "UTF-8". Added in 4.1.0.
- `ONTOP_ENABLE_DOWNLOAD_ONTOLOGY` (optional). Corresponds to the argument`--enable-download-ontology`. Added in 4.2.0.

## Tutorial

A tutorial is provided on the Ontop Website: https://ontop-vkg.org/tutorial/endpoint/endpoint-docker


