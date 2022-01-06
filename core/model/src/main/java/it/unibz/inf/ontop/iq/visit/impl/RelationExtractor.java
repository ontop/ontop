package it.unibz.inf.ontop.iq.visit.impl;

import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;

import java.util.stream.Stream;

public class RelationExtractor extends AbstractPredicateExtractor<ExtensionalDataNode> {

    public RelationExtractor() {
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
