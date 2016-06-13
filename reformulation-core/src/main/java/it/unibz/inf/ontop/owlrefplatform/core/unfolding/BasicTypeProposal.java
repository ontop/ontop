package it.unibz.inf.ontop.owlrefplatform.core.unfolding;

import com.google.common.collect.ImmutableSet;
import fj.P;
import fj.P2;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.TypeProposal;
import it.unibz.inf.ontop.model.Variable;


import java.util.HashSet;
import java.util.Set;

/**
 * Normal case.
 *
 * Does "nothing" (does converse but returns the same body atom).
 *
 */
public class BasicTypeProposal implements TypeProposal {

    private final Function typedAtom;

    public BasicTypeProposal(Function unextendedTypedAtom) {
        this.typedAtom = unextendedTypedAtom;
    }

    @Override
    public Function getExtendedTypedAtom() {
        return typedAtom;
    }

    @Override
    public Predicate getPredicate() {
        return typedAtom.getFunctionSymbol();
    }

    @Override
    public P2<Function, Set<Variable>> convertIntoExtendedAtom(Function bodyAtom, ImmutableSet<Variable> alreadyKnownRuleVariables) {
        return P.p(bodyAtom, (Set<Variable>) new HashSet<Variable>());
    }
}
