package it.unibz.inf.ontop.owlapi.connection;


import it.unibz.inf.ontop.iq.IQ;
import org.semanticweb.owlapi.model.OWLException;

public interface OntopOWLStatement extends OWLStatement {

    String getRewritingRendering(String query) throws OWLException;

    IQ getExecutableQuery(String query) throws OWLException;
}
