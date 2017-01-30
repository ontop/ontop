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
     * Gets the target query.
     */
    ExecutableQuery getExecutableQuery(String query) throws OBDAException;

    /**
     * Gets the string representation of the query before unfolding w.r.t the mapping.
     */
    String getRewriting(String query) throws OBDAException;
}
