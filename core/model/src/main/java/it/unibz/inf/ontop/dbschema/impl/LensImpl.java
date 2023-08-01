package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.Lens;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;

import javax.annotation.Nonnull;

public class LensImpl extends AbstractNamedRelationDefinition implements Lens {
    
    private final int level;
    @Nonnull
    private IQ iq;
    private boolean isFrozen;

    public LensImpl(ImmutableList<RelationID> allIds, AttributeListBuilder builder,
                    IQ iqWithTemporaryAtomPredicate, int level, CoreSingletons coreSingletons) {
        super(allIds, builder);
        this.iq = replaceAtomPredicate(getAtomPredicate(), iqWithTemporaryAtomPredicate, coreSingletons);
        this.level = level;
        if (level < 1)
            throw new IllegalArgumentException("Minimum level for a lens is 1");
        this.isFrozen = false;
    }

    private static IQ replaceAtomPredicate(RelationPredicate newAtomPredicate, IQ iqWithTemporaryAtomPredicate,
                                           CoreSingletons coreSingletons) {
        DistinctVariableOnlyDataAtom newProjectionAtom = coreSingletons.getAtomFactory().getDistinctVariableOnlyDataAtom(
                newAtomPredicate, iqWithTemporaryAtomPredicate.getProjectionAtom().getArguments());

        return coreSingletons.getIQFactory().createIQ(newProjectionAtom, iqWithTemporaryAtomPredicate.getTree());
    }

    @Override
    public IQ getIQ() {
        return iq;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void updateIQ(@Nonnull IQ newIQ) throws IllegalStateException {
        if (isFrozen)
            throw new IllegalStateException("Cannot change the IQ after freezing");
        if (!iq.getProjectionAtom().getPredicate().equals(newIQ.getProjectionAtom().getPredicate()))
            throw new IllegalArgumentException("IQ with a different atom predicate provided");
        this.iq = newIQ;
    }

    @Override
    public void freeze() {
        this.isFrozen = true;
    }

    @Override
    public String toString() {
        return "Lens " + getID();
    }
}
