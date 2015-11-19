package org.semanticweb.ontop.owlrefplatform.core;

import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAStatement;
import org.semanticweb.ontop.ontology.Assertion;

import java.net.URI;
import java.util.Iterator;

/**
 * High-level OBDAStatement class used by Sesame.
 *
 * TODO: Rename it (not now) QuestDBStatement.
 */
public interface IQuestDBStatement extends OBDAStatement {

    /**
     * Deprecated. See getTargetQuery() instead.
     */
    @Deprecated
    String getSQL(String query) throws OBDAException;

    /**
     * Gets the target query.
     */
    ExecutableQuery getTargetQuery(String query) throws OBDAException;

    /**
     * TODO: explain
     */
    String getRewriting(String query) throws OBDAException;

    /**
     * May not be supported (if read-only)
     */
    int add(Iterator<Assertion> data) throws OBDAException;

    /**
     * May not be supported (if read-only)
     */
    int add(Iterator<Assertion> data, int commit, int batch) throws OBDAException;

    /**
     * May not be supported (if read-only)
     */
    int add(URI rdffile) throws OBDAException;

    /**
     * May not be supported (if read-only)
     */
    int addWithTempFile(URI rdffile) throws OBDAException;

    /**
     * May not be supported (if read-only)
     */
    int addFromOBDA(URI obdaFile) throws OBDAException;
}
