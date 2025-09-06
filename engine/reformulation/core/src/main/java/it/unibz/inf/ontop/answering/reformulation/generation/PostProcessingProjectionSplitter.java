package it.unibz.inf.ontop.answering.reformulation.generation;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.splitter.ProjectionSplitter;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface PostProcessingProjectionSplitter extends ProjectionSplitter {

    ProjectionSplit split(IQTree tree, VariableGenerator variableGenerator, boolean avoidPostProcessing);

}
