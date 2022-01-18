package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public class TargetAtomImpl implements TargetAtom {

    protected final DistinctVariableOnlyDataAtom atom;
    protected final ImmutableSubstitution<ImmutableTerm> substitution;

    protected TargetAtomImpl(DistinctVariableOnlyDataAtom atom, ImmutableSubstitution<ImmutableTerm> substitution) {
        this.atom = atom;
        this.substitution = substitution;
    }

    @Override
    public DistinctVariableOnlyDataAtom getProjectionAtom() {
        return atom;
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public ImmutableTerm getSubstitutedTerm(int index) {
        return substitution.applyToVariable(atom.getTerm(index));
    }

    @Override
    public ImmutableList<ImmutableTerm> getSubstitutedTerms() {
        return atom.getArguments().stream()
                .map(substitution::applyToVariable)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public TargetAtom changeSubstitution(ImmutableSubstitution<ImmutableTerm> newSubstitution) {
        return new TargetAtomImpl(atom, newSubstitution);
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
