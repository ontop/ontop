package org.semanticweb.ontop.owlrefplatform.core;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.nativeql.DBMetadataException;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;

import javax.annotation.Nullable;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * TODO: describe
 * TODO: find a better name?
 */
public interface DBConnector {

    /**
     * TODO: keep it public?
     */
    boolean connect() throws SQLException;
    void disconnect() throws SQLException;
    void dispose();
    void close();


    DBMetadata extractDBMetadata(OBDAModel obdaModel, @Nullable ImplicitDBConstraints userConstraints)
            throws DBMetadataException;

    OBDAModel expandMetaMappings(OBDAModel unfoldingOBDAModel, URI sourceId) throws Exception;

    void preprocessProjection(ImmutableList<OBDAMappingAxiom> mappings) throws SQLException;

    IQuestConnection getNonPoolConnection() throws OBDAException;
    IQuestConnection getConnection() throws OBDAException;

    @Deprecated
    Connection getSQLPoolConnection() throws OBDAException;

    void releaseSQLPoolConnection(Connection connection);
}
