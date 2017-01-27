package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAStatement;
import it.unibz.inf.ontop.ontology.Assertion;

import java.net.URI;
import java.util.Iterator;

/**
 * High-level OBDAStatement class used by Sesame.
 *
 * TODO: Rename it (not now) QuestDBStatement.
 */
public interface IQuestDBStatement extends OBDAStatement {

    /**
     * Deprecated. See getExecutableQuery() instead.
     */
    @Deprecated
    String getSQL(String query) throws OBDAException;

    /**
     * Gets the target query.
     */
    ExecutableQuery getExecutableQuery(String query) throws OBDAException;

    /**
     * TODO: explain
     */
    String getRewriting(String query) throws OBDAException;
}
