package it.unibz.inf.ontop.owlapi.impl;

import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.impl.DefaultOntopOWLConnection;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;


public class SimpleOntopOWLEngine implements OntopOWLEngine {

    private final OntopQueryEngine queryEngine;
    private final KGQueryFactory kgQueryFactory;

    public SimpleOntopOWLEngine(OntopSystemConfiguration configuration) throws InvalidOBDASpecificationException {
        try {
            this.queryEngine = configuration.loadQueryEngine();
            kgQueryFactory = configuration.getKGQueryFactory();
        } catch (OBDASpecificationException e) {
            throw new InvalidOBDASpecificationException(e); //, new QuestOWLConfiguration(configuration));
        }
    }

    @Override
    public OntopOWLConnection getConnection() throws ReasonerInternalException {
        try {
            OntopConnection conn = queryEngine.getConnection();
            return new DefaultOntopOWLConnection(conn, kgQueryFactory);
        }
        catch (OntopConnectionException e) {
            // TODO: find a better exception?
            throw new ReasonerInternalException(e);
        }
    }

    @Override
    public void close() throws Exception {
        queryEngine.close();
    }

    public static class InvalidOBDASpecificationException extends RuntimeException {
        public InvalidOBDASpecificationException(OBDASpecificationException e) {
            super(e);
        }
    }
}
