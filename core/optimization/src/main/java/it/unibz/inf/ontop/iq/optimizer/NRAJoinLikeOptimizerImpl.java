package it.unibz.inf.ontop.iq.optimizer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.impl.FixedPointJoinLikeOptimizer;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NRAJoinLikeOptimizerImpl implements NRAJoinLikeOptimizer {

    private static final Logger log = LoggerFactory.getLogger(FixedPointJoinLikeOptimizer.class);
    private final FixedPointJoinLikeOptimizer joinlikeOptimizer;
    private final LevelUpOptimizer levelUpOptimizer;
    private final FlattenLifter flattenLifter;
    private final IQConverter iqConverter;

    @Inject
    private NRAJoinLikeOptimizerImpl(FixedPointJoinLikeOptimizer joinlikeOptimizer, LevelUpOptimizer levelUpOptimizer,
                                     FlattenLifter flattenLifter, IQConverter iqConverter) {
        this.joinlikeOptimizer = joinlikeOptimizer;
        this.levelUpOptimizer = levelUpOptimizer;
        this.flattenLifter = flattenLifter;
        this.iqConverter = iqConverter;
    }

    @Override
    public IQ optimize(IQ query, ExecutorRegistry executorRegistry) throws EmptyQueryException {
        IQ formerQuery = query;
        do {
            query = flattenLifter.optimize(query);
            log.debug("New query after flatten lift: \n" + query.toString());

            IntermediateQuery intermediateQuery = joinlikeOptimizer.optimize(iqConverter.convert(query, executorRegistry));
            log.debug("New query after join optimization: \n" + query.toString());

            query = levelUpOptimizer.optimize(iqConverter.convert(intermediateQuery));
            log.debug("New query after levelUp: \n" + query.toString());

        } while (formerQuery != query);
        return query;
    }
}

