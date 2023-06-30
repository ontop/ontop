package it.unibz.inf.ontop.answering.reformulation.generation;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.splitter.ProjectionSplitter;

public interface PostProcessingProjectionSplitter extends ProjectionSplitter {

    ProjectionSplit split(IQ iq, boolean avoidPostProcessing);

}
