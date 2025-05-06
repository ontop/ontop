package it.unibz.inf.ontop.iq.visit.impl;

import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;

import java.util.stream.Stream;

public class ExtensionalDataNodeExtractor extends AbstractIQTreeToStreamVisitingTransformer<ExtensionalDataNode> {

    @Override
    public Stream<ExtensionalDataNode> transformExtensionalData(ExtensionalDataNode dataNode) {
        return Stream.of(dataNode);
    }
}
