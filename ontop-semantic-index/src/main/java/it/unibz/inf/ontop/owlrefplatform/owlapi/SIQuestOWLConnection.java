package it.unibz.inf.ontop.owlrefplatform.owlapi;

import it.unibz.inf.ontop.owlrefplatform.core.IQuestConnection;
import org.semanticweb.owlapi.model.OWLException;

public class SIQuestOWLConnection extends QuestOWLConnection {

    public SIQuestOWLConnection(IQuestConnection conn) {
        super(conn);
    }

    /**
     * For the classic A-box mode
     */
    public SIQuestOWLStatement createSIStatement() throws OWLException {
        throw new RuntimeException("TODO: re-enable it");
//		try {
//			return new SIQuestOWLStatementImpl(conn.createSIStatement(), this);
//		} catch (OBDAException e) {
//			throw new OWLException(e);
//		}
    }
}
