package org.semanticweb.ontop.owlrefplatform.core.execution;

import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.owlrefplatform.core.IQuestStatement;


/**
 * Special QuestStatement interface for Semantic Index repositories.
 *
 */
public interface SIQuestStatement extends IQuestStatement {

    void createIndexes() throws OBDAException;

    void dropIndexes() throws OBDAException;

    boolean isIndexed();

    void dropRepository() throws OBDAException;

    public void createDB() throws OBDAException;
    public void analyze() throws OBDAException;
}
