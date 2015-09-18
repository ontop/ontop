package org.semanticweb.ontop.owlrefplatform.core;

import org.semanticweb.ontop.model.OBDAException;

import java.sql.SQLException;

/**
 * Special QuestDBStatement for a Semantic Index repository.
 */
public interface SIQuestDBStatement extends IQuestDBStatement {

    void createIndexes() throws OBDAException;

    void dropIndexes() throws OBDAException;

    boolean isIndexed();

    void dropRepository() throws OBDAException;

    void createDB() throws OBDAException;
}
