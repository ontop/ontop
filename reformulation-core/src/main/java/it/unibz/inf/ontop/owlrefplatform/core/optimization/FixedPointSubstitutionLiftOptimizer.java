package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class FixedPointSubstitutionLiftOptimizer implements SubstitutionLiftOptimizer {

    private static final Logger log = LoggerFactory.getLogger(FixedPointSubstitutionLiftOptimizer.class);

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        int oldVersionNumber;
        int countVersion = 0;

        do {
            oldVersionNumber = query.getVersionNumber();

            TopDownSubstitutionLiftOptimizer substLiftOptimizer = new TopDownSubstitutionLiftOptimizer();
            query = substLiftOptimizer.optimize(query);
            log.debug("New query after substitution lift optimization: \n" + query.toString());
            countVersion++;

        } while( oldVersionNumber != query.getVersionNumber() && countVersion != 4 );

        if(countVersion == 4){
            throw new IllegalStateException("Too many substitution lift optimizations are required");
        }

        return query;
    }
}
