package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import com.google.common.collect.ImmutableSet;
import fj.P;
import fj.P2;
import org.semanticweb.ontop.model.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Normal case.
 */
public class BasicTypeProposal extends TypeProposalImpl {

    public BasicTypeProposal(Function typeProposal) {
        super(typeProposal);
    }

    @Override
    public Function getUnifiableAtom() {
        return getProposedAtom();
    }

    @Override
    public P2<Function, Set<Variable>> convertIntoUnifiableAtom(Function bodyAtom, ImmutableSet<Variable> alreadyKnownRuleVariables) {
        return P.p(bodyAtom, (Set<Variable>) new HashSet<Variable>());
    }
}
