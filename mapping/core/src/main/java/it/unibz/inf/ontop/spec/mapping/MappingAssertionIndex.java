package it.unibz.inf.ontop.spec.mapping;

import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import org.apache.commons.rdf.api.IRI;

import java.util.Objects;
import java.util.Optional;

public class MappingAssertionIndex {
    private final boolean isClass;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<IRI> iri; // iri can be empty in meta-mappings or facts with blank nodes for the class
    private final RDFAtomPredicate predicate;

    private MappingAssertionIndex(RDFAtomPredicate predicate, Optional<IRI> iri, boolean isClass) {
        this.predicate = Objects.requireNonNull(predicate);
        this.iri = iri;
        this.isClass = isClass;
    }

    public static MappingAssertionIndex ofProperty(RDFAtomPredicate predicate, Optional<IRI> iri) {
        return new MappingAssertionIndex(predicate, iri, false);
    }

    public static MappingAssertionIndex ofClass(RDFAtomPredicate predicate, Optional<IRI> iri) {
        return new MappingAssertionIndex(predicate, iri, true);
    }

    public static MappingAssertionIndex ofProperty(RDFAtomPredicate predicate, IRI iri) {
        return new MappingAssertionIndex(predicate, Optional.of(iri), false);
    }

    public static MappingAssertionIndex ofClass(RDFAtomPredicate predicate, IRI iri) {
        return new MappingAssertionIndex(predicate, Optional.of(iri), true);
    }

    public boolean isClass() {
        return isClass;
    }

    public IRI getIri() {
        return iri
                .orElseThrow(() -> new MappingAssertion.NoGroundPredicateOntopInternalBugException("The definition of the predicate is not always a ground term"));
    }

    public RDFAtomPredicate getPredicate() { return predicate; }

    @Override
    public int hashCode() { return Objects.hash(iri, predicate); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MappingAssertionIndex) {
            MappingAssertionIndex other = (MappingAssertionIndex)o;
            return predicate.equals(other.predicate) && iri.equals(other.iri) && isClass == other.isClass;
        }
        return false;
    }

    @Override
    public String toString() { return predicate + ":" + (isClass ? "C/" : "P/") + iri; }
}
