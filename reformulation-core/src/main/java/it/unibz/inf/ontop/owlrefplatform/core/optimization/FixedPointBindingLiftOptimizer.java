package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class FixedPointBindingLiftOptimizer implements BindingLiftOptimizer {

    private static final Logger log = LoggerFactory.getLogger(FixedPointBindingLiftOptimizer.class);
    private static final int LOOPS = 10;

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        int oldVersionNumber;
        int countVersion = 0;

        do {
            oldVersionNumber = query.getVersionNumber();

            TopDownBindingLiftOptimizer substLiftOptimizer = new TopDownBindingLiftOptimizer();
            query = substLiftOptimizer.optimize(query);
            log.debug("New query after substitution lift optimization: \n" + query.toString());
            countVersion++;

            if(countVersion == LOOPS){
                throw new IllegalStateException("Too many substitution lift optimizations are executed");
            }

        } while( oldVersionNumber != query.getVersionNumber() );



        return query;
    }
}
