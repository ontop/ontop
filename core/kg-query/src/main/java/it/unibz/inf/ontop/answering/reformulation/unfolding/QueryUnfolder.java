package it.unibz.inf.ontop.answering.reformulation.unfolding;

import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;
import it.unibz.inf.ontop.spec.mapping.Mapping;

/**
 * TODO: explain
 *
 * See QueryUnfolder.Factory for creating a new instance.
 */
public interface QueryUnfolder extends IQOptimizer {


    interface Factory {
        QueryUnfolder create(Mapping mapping);
    }
}
