package it.unibz.inf.ontop.spec.mapping;

/**
 * Methods moved away from OBDADataFactory
 */
public interface SQLMappingFactory {

    OBDASQLQuery getSQLQuery(String query);
}
