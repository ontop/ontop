package it.unibz.inf.ontop.answering.reformulation.generation;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface PostProcessingProjectionSplitter {

    PostProcessingSplit split(IQ iq, boolean avoidPostProcessing);

    interface PostProcessingSplit {
        ConstructionNode getPostProcessingConstructionNode();
        IQTree getSubTree();
        VariableGenerator getVariableGenerator();
    }
}
