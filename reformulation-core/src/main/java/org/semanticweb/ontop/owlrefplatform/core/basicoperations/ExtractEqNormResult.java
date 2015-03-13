package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import fj.data.List;
import org.semanticweb.ontop.model.Function;

/**
 * TODO: explain
 * TODO: find a better name
 */
public class ExtractEqNormResult {
    private final List<Function> nonPushableAtoms;
    private final List<Function> pushableAtoms;
    private final Substitution currentSubstitution;

    public ExtractEqNormResult(List<Function> nonPushableAtoms, List<Function> pushableAtoms, Substitution substitution) {
        this.nonPushableAtoms = nonPushableAtoms;
        this.pushableAtoms = pushableAtoms;
        this.currentSubstitution = substitution;
    }

    public List<Function> getNonPushableAtoms() {
        return nonPushableAtoms;
    }

    public List<Function> getPushableAtoms() {
        return pushableAtoms;
    }

    public Substitution getCurrentSubstitution() {
        return currentSubstitution;
    }

    public List<Function> getAllAtoms() {
        return pushableAtoms.append(nonPushableAtoms);
    }
}
