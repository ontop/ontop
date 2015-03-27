package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import fj.data.List;
import org.semanticweb.ontop.model.Function;

/**
 * TODO: explain
 *
 * Beware: immutable data structures (from the Functional Java library).
 */
public class PullOutEqLocalNormResult {
    private final List<Function> nonPushableAtoms;
    private final List<Function> pushableBoolAtoms;
    private final Var2VarSubstitution substitution;

    public PullOutEqLocalNormResult(List<Function> nonPushableAtoms, List<Function> pushableBoolAtoms,
                                    Var2VarSubstitution var2VarSubstitution) {
        this.nonPushableAtoms = nonPushableAtoms;
        this.pushableBoolAtoms = pushableBoolAtoms;
        this.substitution = var2VarSubstitution;
    }

    public List<Function> getNonPushableAtoms() {
        return nonPushableAtoms;
    }

    public List<Function> getPushableBoolAtoms() {
        return pushableBoolAtoms;
    }

    public List<Function> getAllAtoms() {
        return nonPushableAtoms.append(pushableBoolAtoms);
    }

    public Var2VarSubstitution getVar2VarSubstitution() {
        return substitution;
    }
}
