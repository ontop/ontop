package it.unibz.inf.ontop.iq.transform;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * TODO: find a better name
 */
public interface IQTree2NativeNodeGenerator {

    /**
     * NB: the tolerateUnknownTypes option has been added for the MetaMappingExpander, because
     * at this stage we may not be able to infer the types.
     */
    NativeNode generate(IQTree iqTree, DBParameters dbParameters, boolean tolerateUnknownTypes);

    /**
     * Controls the order of returned variables through the signature
     */
    NativeNode generate(IQTree iqTree, ImmutableSet<Variable> signature, DBParameters dbParameters, boolean tolerateUnknownTypes);

}
