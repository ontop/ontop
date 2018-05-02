package it.unibz.inf.ontop.model.atom.impl;

import it.unibz.inf.ontop.model.atom.Context;
import org.apache.commons.rdf.api.IRI;

public class SimpleNamedGraph implements Context {

    private final IRI graphIri;

    protected SimpleNamedGraph(IRI graphIri) {
        this.graphIri = graphIri;
    }

    @Override
    public IRI getGraphIRI() {
        return graphIri;
    }
}
