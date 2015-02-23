package org.semanticweb.ontop.owlrefplatform.owlapi3;

import org.semanticweb.ontop.model.OBDAException;

/**
 * QuestOWLStatement for managing a Semantic Index repository.
 */
public interface SIQuestOWLStatement extends IQuestOWLStatement {

    public void createIndexes() throws OBDAException;

    public void dropIndexes() throws OBDAException;

    void analyze() throws OBDAException;
}
