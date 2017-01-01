package it.unibz.inf.ontop.sesame;

import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBConnection;

public class OntopClassicRepositoryConnection extends RepositoryConnection {

    public OntopClassicRepositoryConnection(SesameAbstractRepo rep, QuestDBConnection connection) throws OBDAException {
        super(rep, connection);
    }
}
