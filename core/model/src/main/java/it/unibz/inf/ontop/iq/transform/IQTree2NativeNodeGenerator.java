package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;

/**
 * TODO: find a better name
 */
public interface IQTree2NativeNodeGenerator {

    NativeNode generate(IQTree iqTree, DBParameters dbParameters);

}
