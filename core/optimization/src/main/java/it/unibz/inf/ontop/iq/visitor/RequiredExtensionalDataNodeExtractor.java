package it.unibz.inf.ontop.iq.visitor;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;

import java.util.stream.Stream;

public interface RequiredExtensionalDataNodeExtractor  {

    Stream<ExtensionalDataNode> transform(IQTree tree);

}
