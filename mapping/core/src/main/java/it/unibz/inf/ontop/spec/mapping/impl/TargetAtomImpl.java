package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public class TargetAtomImpl implements TargetAtom {

    protected final DistinctVariableOnlyDataAtom atom;
    protected final Substitution<ImmutableTerm> substitution;

    protected TargetAtomImpl(DistinctVariableOnlyDataAtom atom, Substitution<ImmutableTerm> substitution) {
        this.atom = atom;
        this.substitution = substitution;
    }

    @Override
    public DistinctVariableOnlyDataAtom getProjectionAtom() {
        return atom;
    }

    @Override
    public Substitution<ImmutableTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public ImmutableTerm getSubstitutedTerm(int index) {
        return substitution.apply(atom.getTerm(index));
    }

    @Override
    public ImmutableList<ImmutableTerm> getSubstitutedTerms() {
        return substitution.apply(atom.getArguments());
    }
    
    @Override
    public Optional<IRI> getPredicateIRI() {
        return Optional.of(atom.getPredicate())
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate) p)
                .flatMap(p -> p.getPredicateIRI(getSubstitutedTerms()));
    }

    @Override
    public String toString() {
        return atom.toString() + " with " + substitution.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TargetAtomImpl) {
            TargetAtomImpl targetAtom = (TargetAtomImpl)other;
            return this.atom.equals(targetAtom.atom) && this.substitution.equals(targetAtom.substitution);
        }
        return false;
    }
}
