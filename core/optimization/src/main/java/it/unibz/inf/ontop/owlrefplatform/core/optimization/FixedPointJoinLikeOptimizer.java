package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Singleton
public class FixedPointJoinLikeOptimizer implements JoinLikeOptimizer {

    private static final Logger log = LoggerFactory.getLogger(FixedPointJoinLikeOptimizer.class);
    private final InnerJoinOptimizer joinOptimizer;
    private final LeftJoinOptimizer leftJoinOptimizer;

    @Inject
    private FixedPointJoinLikeOptimizer(InnerJoinOptimizer joinOptimizer, LeftJoinOptimizer leftJoinOptimizer){
        this.joinOptimizer = joinOptimizer;
        this.leftJoinOptimizer = leftJoinOptimizer;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        int oldVersionNumber;
        do {
            oldVersionNumber = query.getVersionNumber();
            query = leftJoinOptimizer.optimize(query);
            log.debug("New query after left join optimization: \n" + query.toString());

            query = joinOptimizer.optimize(query);
            log.debug("New query after join optimization: \n" + query.toString());

        } while(oldVersionNumber != query.getVersionNumber());

        return query;
    }
}
