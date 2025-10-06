package it.unibz.inf.ontop.iq.visitor;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;

import java.util.stream.Stream;

/**
 * Looks for extensional data nodes that are required to provide tuples.
 *
 * For instance, excludes data atoms only appearing on the right of a LJ.
 *
 * MAY BE INCOMPLETE
 *
 */

public interface RequiredExtensionalDataNodeExtractor  {

    Stream<ExtensionalDataNode> transform(IQTree tree);

}
