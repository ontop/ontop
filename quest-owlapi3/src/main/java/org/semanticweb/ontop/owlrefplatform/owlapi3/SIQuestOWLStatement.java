package org.semanticweb.ontop.owlrefplatform.owlapi3;

import org.semanticweb.ontop.model.OBDAException;

import java.io.File;

/**
 * QuestOWLStatement for managing a Semantic Index repository.
 */
public interface SIQuestOWLStatement extends IQuestOWLStatement {

    public void createIndexes() throws OBDAException;

    public void dropIndexes() throws OBDAException;

    int insertData(File owlFile, int commitSize, int batchsize, String baseURI) throws Exception;
}
