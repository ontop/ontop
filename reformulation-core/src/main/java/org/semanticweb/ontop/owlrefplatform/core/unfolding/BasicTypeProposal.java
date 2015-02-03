package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.F;
import fj.data.List;
import org.semanticweb.ontop.model.*;

import java.util.ArrayList;

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

    /**
     * NO EFFECT because this type proposal cannot provoke any arity change.
     */
    @Override
    @Deprecated
    public List<CQIE> propagateChildArityChangeToBodies(List<CQIE> initialRules) {
        return initialRules;
    }

    @Override
    public Function prepareBodyAtomForUnification(Function bodyAtom, java.util.Set<Variable> alreadyKnownRuleVariables) {
        return bodyAtom;
    }
}
