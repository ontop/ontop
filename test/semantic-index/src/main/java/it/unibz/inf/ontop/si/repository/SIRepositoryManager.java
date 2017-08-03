package it.unibz.inf.ontop.si.repository;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.si.repository.impl.RepositoryChangedListener;
import it.unibz.inf.ontop.si.repository.impl.SemanticIndexURIMap;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.ontology.Assertion;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Created by benjamin on 28/07/17.
 */
public interface SIRepositoryManager extends Serializable {
    void generateMetadata();

    void addRepositoryChangedListener(RepositoryChangedListener list);

    SemanticIndexURIMap getUriMap();

    void createDBSchemaAndInsertMetadata(Connection conn) throws SQLException;

    void dropDBSchema(Connection conn) throws SQLException;

    int insertData(Connection conn, Iterator<Assertion> data, int commitLimit, int batchLimit) throws SQLException;

    void loadMetadata(Connection conn) throws SQLException;

    ImmutableList<SQLPPTriplesMap> getMappings();

    void insertMetadata(Connection conn) throws SQLException;

    void createIndexes(Connection conn) throws SQLException;


    void dropIndexes(Connection conn) throws SQLException;

    boolean isIndexed(Connection conn);
}
