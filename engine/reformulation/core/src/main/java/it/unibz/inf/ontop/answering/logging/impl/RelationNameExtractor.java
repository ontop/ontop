package it.unibz.inf.ontop.answering.logging.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;

@Singleton
public class RelationNameExtractor extends AbstractPredicateExtractor<ExtensionalDataNode> {

    @Inject
    protected RelationNameExtractor() {
    }

    public ImmutableSet<String> extractRelationNames(IQ iq) {
        return iq.getTree().acceptVisitor(this)
                // TODO: improve the way to get the name
                .map(n -> n.getRelationDefinition().getAtomPredicate().getName())
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public Stream<ExtensionalDataNode> visitIntensionalData(IntensionalDataNode dataNode) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> visitExtensionalData(ExtensionalDataNode dataNode) {
        return Stream.of(dataNode);
    }

}
