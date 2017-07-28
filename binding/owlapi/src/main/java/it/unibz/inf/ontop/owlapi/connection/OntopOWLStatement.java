package it.unibz.inf.ontop.owlapi.connection;


import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import org.semanticweb.owlapi.model.OWLException;

public interface OntopOWLStatement extends OWLStatement {

    String getRewritingRendering(String query) throws OWLException;

    ExecutableQuery getExecutableQuery(String query) throws OWLException;
}
