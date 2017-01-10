package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class FixedPointJoinLikeOptimizer implements JoinLikeOptimizer {

    private static final Logger log = LoggerFactory.getLogger(FixedPointJoinLikeOptimizer.class);

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        int oldVersionNumber;
        do {
            oldVersionNumber = query.getVersionNumber();

            BasicLeftJoinOptimizer leftJoinOptimizer = new BasicLeftJoinOptimizer();
            query = leftJoinOptimizer.optimize(query);
            log.debug("New query after left join optimization: \n" + query.toString());

            int version=query.getVersionNumber();

            BasicJoinOptimizer joinOptimizer = new BasicJoinOptimizer();
            query = joinOptimizer.optimize(query);
            log.debug("New query after join optimization: \n" + query.toString());

        } while(oldVersionNumber != query.getVersionNumber());

        return query;
    }
}
