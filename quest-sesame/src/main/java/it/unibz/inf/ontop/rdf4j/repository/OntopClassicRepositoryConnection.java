package it.unibz.inf.ontop.rdf4j.repository;

import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBConnection;

public class OntopClassicRepositoryConnection extends OntopRepositoryConnection {

    public OntopClassicRepositoryConnection(AbstractOntopRepository rep, QuestDBConnection connection) throws OBDAException {
        super(rep, connection);
    }
}
