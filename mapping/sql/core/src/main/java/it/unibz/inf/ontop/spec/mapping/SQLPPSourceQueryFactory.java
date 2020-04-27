package it.unibz.inf.ontop.spec.mapping;

/**
 * Methods moved away from OBDADataFactory
 */
public interface SQLPPSourceQueryFactory {

    SQLPPSourceQuery createSourceQuery(String query);
}
