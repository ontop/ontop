package it.unibz.inf.ontop.spec.mapping;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;

public class MappingAssertion {

    private final MappingAssertionIndex index;
    private final IQ query;
    private final PPMappingAssertionProvenance provenance;

    public MappingAssertion(MappingAssertionIndex index, IQ query, PPMappingAssertionProvenance provenance) {
        this.index = index;
        this.query = query;
        this.provenance = provenance;
    }

    public MappingAssertionIndex getIndex() { return index; }

    public IQ getQuery() { return query; }

    public PPMappingAssertionProvenance getProvenance() { return provenance; }
}
