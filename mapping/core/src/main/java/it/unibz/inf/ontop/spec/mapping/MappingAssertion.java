package it.unibz.inf.ontop.spec.mapping;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
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

    public MappingAssertion copyOf(IQ query) {
        if (!index.getPredicate().equals(query.getProjectionAtom().getPredicate()))
            throw new MinorOntopInternalBugException("Different atoms predicates: "
                    + index.getPredicate()
                    + " and "
                    + query.getProjectionAtom().getPredicate());

        return new MappingAssertion(index, query, provenance);
    }

    public MappingAssertionIndex getIndex() { return index; }

    public IQ getQuery() { return query; }

    public PPMappingAssertionProvenance getProvenance() { return provenance; }
}
