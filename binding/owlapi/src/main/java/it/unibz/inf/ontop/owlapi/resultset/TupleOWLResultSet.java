package it.unibz.inf.ontop.owlapi.resultset;

import org.semanticweb.owlapi.model.*;

import java.util.List;

public interface TupleOWLResultSet extends IterableOWLResultSet<OWLBindingSet> {

    int getColumnCount() throws OWLException;

    List<String> getSignature() throws OWLException;

    int getFetchSize() throws OWLException;

}
