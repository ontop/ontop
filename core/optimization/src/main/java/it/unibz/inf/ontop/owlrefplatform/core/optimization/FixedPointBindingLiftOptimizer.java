package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class FixedPointBindingLiftOptimizer implements BindingLiftOptimizer {

    private final TrueNodesRemovalOptimizer trueNodesRemovalOptimizer;
    private static final Logger log = LoggerFactory.getLogger(FixedPointBindingLiftOptimizer.class);
    private static final int LOOPS = 10;

    @Inject
    private FixedPointBindingLiftOptimizer(TrueNodesRemovalOptimizer trueNodesRemovalOptimizer) {
        this.trueNodesRemovalOptimizer = trueNodesRemovalOptimizer;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        int oldVersionNumber;
        int countVersion = 0;

        do {
            oldVersionNumber = query.getVersionNumber();

            TopDownBindingLiftOptimizer substLiftOptimizer = new TopDownBindingLiftOptimizer();
            query = substLiftOptimizer.optimize(query);
            log.trace("New query after substitution lift optimization: \n" + query.toString());
            countVersion++;

            if(countVersion == LOOPS){
                throw new IllegalStateException("Too many substitution lift optimizations are executed");
            }

        } while( oldVersionNumber != query.getVersionNumber() );

        return  trueNodesRemovalOptimizer.optimize(query);
    }
}
