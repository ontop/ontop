package it.unibz.inf.ontop.iq.node.normalization;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.utils.VariableGenerator;


public interface OrderByNormalizer {

    IQTree normalizeForOptimization(OrderByNode orderByNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache);
}
