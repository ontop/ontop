package it.unibz.inf.ontop.iq.visit.impl;

import it.unibz.inf.ontop.iq.node.IntensionalDataNode;

import java.util.stream.Stream;

public class IntensionalDataNodeExtractor extends AbstractIQTreeToStreamVisitingTransformer<IntensionalDataNode> {

    @Override
    public Stream<IntensionalDataNode> transformIntensionalData(IntensionalDataNode dataNode) {
        return Stream.of(dataNode);
    }
}
