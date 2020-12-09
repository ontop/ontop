package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.OntopViewDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.iq.IQ;

public class OntopViewDefinitionImpl extends AbstractNamedRelationDefinition implements OntopViewDefinition {
    
    private final int level;
    private final IQ iq;

    OntopViewDefinitionImpl(ImmutableList<RelationID> allIds, AttributeListBuilder builder,
                            IQ iq, int level) {
        super(allIds, builder);
        this.iq = iq;
        this.level = level;
        if (level < 1)
            throw new IllegalArgumentException("Minimum level for a view is 1");
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
