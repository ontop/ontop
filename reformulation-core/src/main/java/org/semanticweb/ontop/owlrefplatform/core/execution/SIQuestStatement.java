package org.semanticweb.ontop.owlrefplatform.core.execution;

import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.owlrefplatform.core.IQuestStatement;


/**
 * Special QuestStatement interface for Semantic Index repositories.
 *
 */
public interface SIQuestStatement extends IQuestStatement {

    /***
     * In an ABox store (classic) this methods triggers the generation of the
     * schema and the insertion of the metadata.
     *
     */
    void createIndexes() throws OBDAException;

    void dropIndexes() throws OBDAException;

    boolean isIndexed();

    void dropRepository() throws OBDAException;

    public void createDB() throws OBDAException;

    public void analyze() throws OBDAException;
}
