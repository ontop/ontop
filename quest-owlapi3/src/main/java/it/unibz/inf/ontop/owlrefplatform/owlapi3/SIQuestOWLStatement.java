package it.unibz.inf.ontop.owlrefplatform.owlapi3;

import it.unibz.inf.ontop.model.OBDAException;

import java.io.File;

/**
 * QuestOWLStatement for managing a Semantic Index repository.
 */
public interface SIQuestOWLStatement extends IQuestOWLStatement {

    public void createIndexes() throws OBDAException;

    public void dropIndexes() throws OBDAException;

    int insertData(File owlFile, int commitSize, int batchsize, String baseURI) throws Exception;
}
