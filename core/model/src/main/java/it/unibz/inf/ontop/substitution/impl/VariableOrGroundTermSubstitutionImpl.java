package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.VariableOrGroundTermSubstitution;


public class VariableOrGroundTermSubstitutionImpl<T extends VariableOrGroundTerm>
        extends ImmutableSubstitutionImpl<T>
        implements VariableOrGroundTermSubstitution<T> {

    protected VariableOrGroundTermSubstitutionImpl(ImmutableMap<Variable, ? extends T> substitutionMap,
                                                   AtomFactory atomFactory, TermFactory termFactory) {
        super(substitutionMap, atomFactory, termFactory);
    }

    @Override
    public VariableOrGroundTermSubstitution<VariableOrGroundTerm> composeWith2(
            ImmutableSubstitution<? extends VariableOrGroundTerm> g) {

        ImmutableSubstitution<VariableOrGroundTerm> composedSubstitution = (ImmutableSubstitution<VariableOrGroundTerm>)
                (ImmutableSubstitution<?>) composeWith(g);
        return new VariableOrGroundTermSubstitutionImpl<>(composedSubstitution.getImmutableMap(),
                getAtomFactory(), getTermFactory());
    }
}
