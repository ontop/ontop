package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.InnerJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;


@Singleton
public class FixedPointJoinLikeOptimizer implements JoinLikeOptimizer {

    private static final int MAX_LOOP = 100;
    private final InnerJoinIQOptimizer innerJoinIQOptimizer;
    private final LeftJoinIQOptimizer leftJoinIQOptimizer;

    @Inject
    private FixedPointJoinLikeOptimizer(InnerJoinIQOptimizer innerJoinIQOptimizer, LeftJoinIQOptimizer leftJoinIQOptimizer){
        this.innerJoinIQOptimizer = innerJoinIQOptimizer;
        this.leftJoinIQOptimizer = leftJoinIQOptimizer;
    }

    @Override
    public IQ optimize(IQ initialIQ) {
        // Non-final
        IQ currentIQ = initialIQ;
        for (int i = 0; i < MAX_LOOP; i++) {
            IQ innerJoinOptimizedIQ = innerJoinIQOptimizer.optimize(currentIQ);
            IQ leftJoinOptimizedIQ = leftJoinIQOptimizer.optimize(innerJoinOptimizedIQ);
            IQ optimizedIQ = leftJoinOptimizedIQ.normalizeForOptimization();
            if (optimizedIQ.equals(currentIQ))
                return optimizedIQ;
            else
                currentIQ = optimizedIQ;
        }
        throw new MinorOntopInternalBugException("MAX_LOOP reached");
    }
}
