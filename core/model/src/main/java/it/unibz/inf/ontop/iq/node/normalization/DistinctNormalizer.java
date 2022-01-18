package it.unibz.inf.ontop.iq.node.normalization;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface DistinctNormalizer {

    IQTree normalizeForOptimization(DistinctNode distinctNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache);
}
