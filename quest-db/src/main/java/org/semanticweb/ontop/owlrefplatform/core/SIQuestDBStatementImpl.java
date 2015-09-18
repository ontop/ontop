package org.semanticweb.ontop.owlrefplatform.core;

import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.owlrefplatform.core.execution.SIQuestStatement;

/**
 * QuestDBStatement for Semantic Index repositories.
 */
public class SIQuestDBStatementImpl extends QuestDBStatement implements SIQuestDBStatement {

    private final SIQuestStatement st;

    public SIQuestDBStatementImpl(SIQuestStatement st, NativeQueryLanguageComponentFactory nativeQLFactory) {
        super(st, nativeQLFactory);
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
    public boolean isIndexed() {
        return st.isIndexed();
    }

    @Override
    public void dropRepository() throws OBDAException {
        st.dropRepository();
    }

    /***
     * In an ABox store (classic) this methods triggers the generation of the
     * schema and the insertion of the metadata.
     */
    @Override
    public void createDB() throws OBDAException {
        st.createDB();
    }
}
