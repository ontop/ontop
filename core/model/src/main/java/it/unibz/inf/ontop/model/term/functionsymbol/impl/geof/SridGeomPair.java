package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import it.unibz.inf.ontop.model.term.ImmutableTerm;

public class SridGeomPair {
    final String srid;
    final ImmutableTerm geometry;

    public SridGeomPair(String srid, ImmutableTerm geometry) {
        this.srid = srid;
        this.geometry = geometry;
    }

    public String getSrid() {
        return srid;
    }

    public ImmutableTerm getGeometry() {
        return geometry;
    }
}
