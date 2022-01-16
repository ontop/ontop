package it.unibz.inf.ontop.iq.optimizer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.impl.FixedPointJoinLikeOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NRAJoinLikeOptimizerImpl implements NRAJoinLikeOptimizer {

    private static final Logger log = LoggerFactory.getLogger(FixedPointJoinLikeOptimizer.class);
    private final FixedPointJoinLikeOptimizer joinlikeOptimizer;
    private final LevelUpOptimizer levelUpOptimizer;
    private final FlattenLifter flattenLifter;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private NRAJoinLikeOptimizerImpl(FixedPointJoinLikeOptimizer joinlikeOptimizer, LevelUpOptimizer levelUpOptimizer,
                                     FlattenLifter flattenLifter, IntermediateQueryFactory iqFactory) {
        this.joinlikeOptimizer = joinlikeOptimizer;
        this.levelUpOptimizer = levelUpOptimizer;
        this.flattenLifter = flattenLifter;
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {

        IQ previousQuery;
        IQ optimizedQuery = query;
        do {
            previousQuery = optimizedQuery;

            optimizedQuery = flattenLifter.optimize(optimizedQuery);
            log.debug("New query after flatten lift: \n" + optimizedQuery.toString());

            optimizedQuery = joinlikeOptimizer.optimize(optimizedQuery);
            log.debug("New query after join optimization: \n" + optimizedQuery.toString());

            optimizedQuery = levelUpOptimizer.optimize(optimizedQuery);
            log.debug("New query after levelUp: \n" + optimizedQuery.toString());

        } while (!previousQuery.equals(optimizedQuery));
        return query;
    }
}

