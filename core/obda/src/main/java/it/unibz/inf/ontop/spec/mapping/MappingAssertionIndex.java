package it.unibz.inf.ontop.spec.mapping;

import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import org.apache.commons.rdf.api.IRI;

public class MappingAssertionIndex {
    private final boolean isClass;
    private final IRI iri;
    private final RDFAtomPredicate predicate;

    public MappingAssertionIndex(RDFAtomPredicate predicate, IRI iri, boolean isClass) {
        this.predicate = predicate;
        this.iri = iri;
        this.isClass = isClass;
    }

    public boolean isClass() {
        return isClass;
    }

    public IRI getIri() {
        return iri;
    }

    public RDFAtomPredicate getPredicate() { return predicate; }

    @Override
    public int hashCode() { return iri.hashCode() ^ predicate.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MappingAssertionIndex) {
            MappingAssertionIndex other = (MappingAssertionIndex)o;
            return predicate.equals(other.predicate) && iri.equals(other.iri) && isClass == other.isClass;
        }
        return false;
    }

    @Override
    public String toString() { return predicate + ":" + (isClass ? "C/" : "P/") + iri; }
}
