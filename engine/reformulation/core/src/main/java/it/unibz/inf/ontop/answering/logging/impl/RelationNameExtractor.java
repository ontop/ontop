package it.unibz.inf.ontop.answering.logging.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.visit.impl.ExtensionalDataNodeExtractor;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

@Singleton
public class RelationNameExtractor {

    private final ExtensionalDataNodeExtractor relationExtractor;

    @Inject
    protected RelationNameExtractor(ExtensionalDataNodeExtractor relationExtractor) {
        this.relationExtractor = relationExtractor;
    }

    public ImmutableSet<String> extractRelationNames(IQ iq) {
        return iq.getTree().acceptVisitor(relationExtractor)
                // TODO: improve the way to get the name
                .map(n -> n.getRelationDefinition().getAtomPredicate().getName())
                .collect(ImmutableCollectors.toSet());
    }
}
