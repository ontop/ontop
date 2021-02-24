package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.OntopViewDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;

public class OntopViewDefinitionImpl extends AbstractNamedRelationDefinition implements OntopViewDefinition {
    
    private final int level;
    private final IQ iq;

    public OntopViewDefinitionImpl(ImmutableList<RelationID> allIds, AttributeListBuilder builder,
                            IQ iqWithTemporaryAtomPredicate, int level, CoreSingletons coreSingletons) {
        super(allIds, builder);
        this.iq = replaceAtomPredicate(getAtomPredicate(), iqWithTemporaryAtomPredicate, coreSingletons);
        this.level = level;
        if (level < 1)
            throw new IllegalArgumentException("Minimum level for a view is 1");
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
}
