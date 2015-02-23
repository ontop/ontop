package org.semanticweb.ontop.owlrefplatform.owlapi3;

import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.owlrefplatform.core.execution.SIQuestStatement;

/**
 * Implementation of a QuestOWLStatement for managing a Semantic Index repository.
 */
public class SIQuestOWLStatementImpl extends QuestOWLStatement implements SIQuestOWLStatement  {

    private SIQuestStatement st;

    public SIQuestOWLStatementImpl(SIQuestStatement st, QuestOWLConnection conn) {
        super(st, conn);
        this.st = st;
    }

    @Override
    public void createIndexes() throws OBDAException {
        st.createIndexes();
    }

    @Override
    public void dropIndexes() throws OBDAException {
        st.dropIndexes();
    }

    @Override
    public void analyze() throws OBDAException {
        st.analyze();

    }
}
