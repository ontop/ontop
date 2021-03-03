package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import org.apache.commons.rdf.api.IRI;

public class WKTLiteralValue {
    private final IRI srid;
    private final ImmutableTerm geometry;

    public WKTLiteralValue(IRI srid, ImmutableTerm geometry) {
        this.srid = srid;
        this.geometry = geometry;
    }

    public IRI getSRID() {
        return srid;
    }

    public ImmutableTerm getGeometry() {
        return geometry;
    }
}
