package it.unibz.inf.ontop.spec.rule.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.visit.impl.AbstractPredicateExtractor;

import java.util.stream.Stream;

public class IntensionalNodeExtractor extends AbstractPredicateExtractor<IntensionalDataNode> {

    @Inject
    protected IntensionalNodeExtractor() {
    }

    @Override
    public Stream<IntensionalDataNode> transformIntensionalData(IntensionalDataNode dataNode) {
        return Stream.of(dataNode);
    }

    @Override
    public Stream<IntensionalDataNode> transformExtensionalData(ExtensionalDataNode dataNode) {
        return Stream.empty();
    }
}
