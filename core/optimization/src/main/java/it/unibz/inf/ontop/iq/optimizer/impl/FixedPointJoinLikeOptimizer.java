package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.iq.optimizer.LevelUpOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


@Singleton
public class FixedPointJoinLikeOptimizer implements JoinLikeOptimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixedPointJoinLikeOptimizer.class);
    private static final int MAX_LOOP = 100;
    private final InnerJoinMutableOptimizer joinMutableOptimizer;
    private final LeftJoinMutableOptimizer leftJoinMutableOptimizer;
    private final InnerJoinIQOptimizer innerJoinIQOptimizer;
    private final LeftJoinIQOptimizer leftJoinIQOptimizer;
    private final IQConverter iqConverter;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private FixedPointJoinLikeOptimizer(InnerJoinMutableOptimizer joinMutableOptimizer, LeftJoinMutableOptimizer leftJoinMutableOptimizer,
                                        InnerJoinIQOptimizer innerJoinIQOptimizer, LeftJoinIQOptimizer leftJoinIQOptimizer,
                                        IQConverter iqConverter, IntermediateQueryFactory iqFactory){
        this.joinMutableOptimizer = joinMutableOptimizer;
        this.leftJoinMutableOptimizer = leftJoinMutableOptimizer;
        this.innerJoinIQOptimizer = innerJoinIQOptimizer;
        this.leftJoinIQOptimizer = leftJoinIQOptimizer;
        this.iqConverter = iqConverter;
        this.iqFactory = iqFactory;
    }

    /**
     * Combines IQ optimizations and "mutable" optimizations
     */
    @Override
    public IQ optimize(IQ initialIQ, ExecutorRegistry executorRegistry) {
        boolean isLogDebugEnabled = LOGGER.isDebugEnabled();

        //Non-final
        IQ iq = initialIQ;

        boolean hasMutableQueryChanged;
        boolean isFirstRound = true;

        try {
            do {
                IQ oldIq = iq;
                iq = optimizeIQ(oldIq);

                if (!isFirstRound)
                    // Converged
                    if (oldIq.equals(iq))
                        return iq;

                IntermediateQuery mutableQuery = iqConverter.convert(iq, executorRegistry);

                UUID initialVersionNumber = mutableQuery.getVersionNumber();
                UUID oldVersionNumber;
                do {
                    oldVersionNumber = mutableQuery.getVersionNumber();
                    mutableQuery = leftJoinMutableOptimizer.optimize(mutableQuery);
                    if (isLogDebugEnabled)
                        LOGGER.debug("New query after left join mutable optimization: \n" + mutableQuery.toString());

                    mutableQuery = joinMutableOptimizer.optimize(mutableQuery);
                    if (isLogDebugEnabled)
                        LOGGER.debug("New query after join mutable optimization: \n" + mutableQuery.toString());

                } while (oldVersionNumber != mutableQuery.getVersionNumber());

                hasMutableQueryChanged = (initialVersionNumber != mutableQuery.getVersionNumber());

                if (hasMutableQueryChanged)
                    iq = iqConverter.convert(mutableQuery);

                isFirstRound = false;
            } while (hasMutableQueryChanged);

        } catch (EmptyQueryException e ) {
            return iqFactory.createIQ(initialIQ.getProjectionAtom(),
                    iqFactory.createEmptyNode(initialIQ.getTree().getVariables()));
        }
        return iq;
    }

    private IQ optimizeIQ(IQ initialIQ) {
        // Non-final
        IQ currentIQ = initialIQ;

        for (int i=0; i < MAX_LOOP; i++){

            IQ optimizedIQ = leftJoinIQOptimizer.optimize(innerJoinIQOptimizer.optimize(currentIQ))
                    .normalizeForOptimization();
            if (optimizedIQ.equals(currentIQ))
                return optimizedIQ;
            else
                currentIQ = optimizedIQ;
        }
        throw new MinorOntopInternalBugException("MAX_LOOP reached");
    }
}
