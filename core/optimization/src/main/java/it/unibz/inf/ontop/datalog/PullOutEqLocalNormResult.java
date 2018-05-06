package it.unibz.inf.ontop.datalog;

import fj.data.List;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;

/**
 * TODO: explain
 *
 * Beware: immutable data structures (from the Functional Java library).
 */
public class PullOutEqLocalNormResult {
    private final List<Function> nonPushableAtoms;
    private final List<Function> pushableBoolAtoms;
    private final Substitution substitution;

    public PullOutEqLocalNormResult(List<Function> nonPushableAtoms, List<Function> pushableBoolAtoms,
                                    Substitution var2VarSubstitution) {
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

    public Substitution getVar2VarSubstitution() {
        return substitution;
    }
}
