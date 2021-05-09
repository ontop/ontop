package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.impl.OntopViewDefinitionImpl;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;

public class OntopFlattenedViewDefinitionImpl extends OntopViewDefinitionImpl {


    public OntopFlattenedViewDefinitionImpl(ImmutableList<RelationID> allIds, AttributeListBuilder builder,
                                   IQ iqWithTemporaryAtomPredicate, int level, CoreSingletons coreSingletons) {
        super(allIds, builder, iqWithTemporaryAtomPredicate, level, coreSingletons);
    }
}
