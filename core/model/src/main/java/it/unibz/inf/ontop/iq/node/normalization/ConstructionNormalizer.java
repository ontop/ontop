package it.unibz.inf.ontop.iq.node.normalization;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface ConstructionNormalizer {
    IQTree normalizeForOptimization(ConstructionNode constructionNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache);
}
