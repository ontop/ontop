package it.unibz.inf.ontop.si.repository;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.si.repository.impl.SemanticIndexURIMap;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.ontology.Assertion;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Stores ABox assertions (triples) in the DB
 */
public interface SIRepositoryManager {

    void generateMetadata();

    SemanticIndexURIMap getUriMap();

    void createDBSchemaAndInsertMetadata(Connection conn) throws SQLException;

    int insertData(Connection conn, Iterator<Assertion> data, int commitLimit, int batchLimit) throws SQLException;

    ImmutableList<SQLPPTriplesMap> getMappings();

    void insertMetadata(Connection conn) throws SQLException;
}
