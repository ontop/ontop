package it.unibz.inf.ontop.answering.reformulation.generation;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;

/**
 * TODO: find a better name
 */
public interface IQTree2NativeNodeGenerator {

    NativeNode generate(IQTree iqTree);

}
