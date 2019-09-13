package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 *
 */
@Singleton
public class FixedPointJoinLikeOptimizer implements JoinLikeOptimizer {

    private static final Logger log = LoggerFactory.getLogger(FixedPointJoinLikeOptimizer.class);
    private static final int MAX_LOOP = 100;
    private final InnerJoinMutableOptimizer joinMutableOptimizer;
    private final LeftJoinMutableOptimizer leftJoinMutableOptimizer;
    private final InnerJoinIQOptimizer innerJoinIQOptimizer;
    private final LeftJoinIQOptimizer leftJoinIQOptimizer;
    private final IQConverter iqConverter;

    @Inject
    private FixedPointJoinLikeOptimizer(InnerJoinMutableOptimizer joinMutableOptimizer, LeftJoinMutableOptimizer leftJoinMutableOptimizer,
                                        InnerJoinIQOptimizer innerJoinIQOptimizer, LeftJoinIQOptimizer leftJoinIQOptimizer, IQConverter iqConverter){
        this.joinMutableOptimizer = joinMutableOptimizer;
        this.leftJoinMutableOptimizer = leftJoinMutableOptimizer;
        this.innerJoinIQOptimizer = innerJoinIQOptimizer;
        this.leftJoinIQOptimizer = leftJoinIQOptimizer;
        this.iqConverter = iqConverter;
    }

    /**
     * Combines "mutable" optimizations and IQ optimizations
     */
    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        UUID conversionVersion = UUID.randomUUID();
        boolean converged;
        do {

            UUID oldVersionNumber;
            do {
                oldVersionNumber = query.getVersionNumber();
                query = leftJoinMutableOptimizer.optimize(query);
                log.debug("New query after left join mutable optimization: \n" + query.toString());

                query = joinMutableOptimizer.optimize(query);
                log.debug("New query after join mutable optimization: \n" + query.toString());

            } while (oldVersionNumber != query.getVersionNumber());

            converged = (conversionVersion == query.getVersionNumber());
            if (!converged) {
                IQ newIQ = optimizeIQ(iqConverter.convert(query));
                query = iqConverter.convert(newIQ, query.getExecutorRegistry());
                conversionVersion = query.getVersionNumber();
            }

        } while (!converged);
        return query;
    }

    private IQ optimizeIQ(IQ initialIQ) {
        // Non-final
        IQ currentIQ = initialIQ;

        for (int i=0; i < MAX_LOOP; i++){

            IQ optimizedIQ = leftJoinIQOptimizer.optimize(innerJoinIQOptimizer.optimize(initialIQ))
                    .normalizeForOptimization();
            if (optimizedIQ.equals(currentIQ))
                return optimizedIQ;
            else
                currentIQ = optimizedIQ;
        }
        throw new MinorOntopInternalBugException("MAX_LOOP reached");
    }
}
