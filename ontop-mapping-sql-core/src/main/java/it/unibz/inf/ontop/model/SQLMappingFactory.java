package it.unibz.inf.ontop.model;

/**
 * Methods moved away from OBDADataFactory
 */
public interface SQLMappingFactory {

    OBDASQLQuery getSQLQuery(String query);
}
