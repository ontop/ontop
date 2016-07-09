package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.model.OBDAException;

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
