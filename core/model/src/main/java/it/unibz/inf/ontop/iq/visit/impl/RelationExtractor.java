package it.unibz.inf.ontop.iq.visit.impl;

import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;

import java.util.stream.Stream;

public class RelationExtractor extends AbstractPredicateExtractor<ExtensionalDataNode> {

    @Override
    public Stream<ExtensionalDataNode> transformIntensionalData(IntensionalDataNode dataNode) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> transformExtensionalData(ExtensionalDataNode dataNode) {
        return Stream.of(dataNode);
    }
}
