package org.semanticweb.ontop.owlrefplatform.core;

import org.semanticweb.ontop.model.OBDAException;

import java.sql.SQLException;

/**
 * TODO: describe
 */
public interface SIQuestDBStatement extends IQuestDBStatement {

    void createIndexes() throws OBDAException;

    void dropIndexes() throws OBDAException;

    boolean isIndexed();

    void dropRepository() throws OBDAException;

    void createDB() throws OBDAException;

    void analyze() throws OBDAException;
}
