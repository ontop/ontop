package it.unibz.inf.ontop.owlrefplatform.owlapi;


import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;
import org.semanticweb.owlapi.model.OWLException;

public interface OntopOWLStatement extends OWLStatement {

    String getRewritingRendering(String query) throws OWLException;

    ExecutableQuery getExecutableQuery(String query) throws OWLException;
}
