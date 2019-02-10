package it.unibz.inf.ontop.dbschema;

/**
 * Metadata about the configuration of the data source.
 *
 * This information is typically extracted by connecting to the data source.
 *
 * DOES NOT CONTAIN information about the SCHEMA
 *
 * TODO: move it to be more general package
 */
public interface DBParameters {

    QuotedIDFactory getQuotedIDFactory();

}
