package it.unibz.inf.ontop.iq.node.normalization;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface FlattenNormalizer {

    /**
     * If the child is a construction node, lift what can be lifted from the substitution
     */
    IQTree normalizeForOptimization(FlattenNode flattenNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache);

}
