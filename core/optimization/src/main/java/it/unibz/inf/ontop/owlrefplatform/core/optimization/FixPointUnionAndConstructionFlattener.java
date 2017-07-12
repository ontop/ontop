package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixPointUnionAndConstructionFlattener implements IntermediateQueryOptimizer{

    private static final Logger log = LoggerFactory.getLogger(FixPointUnionAndConstructionFlattener.class);
    private static final int LOOPS = 10;

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        FlattenUnionOptimizer flattenUnionOptimizer = new FlattenUnionOptimizer();

        int oldVersionNumber;
        int countVersion = 0;

        do {
            oldVersionNumber = query.getVersionNumber();

            query = constructionNodeRemovalOptimizer.optimize(query);
            log.debug("New query after ConstructionNode removal optimization: \n" + query.toString());
            query = flattenUnionOptimizer.optimize(query);
            log.debug("New query after Union flattening optimization: \n" + query.toString());

            countVersion++;

            if(countVersion == LOOPS){
                throw new IllegalStateException("Too many optimization cycles are executed");
            }
        } while( oldVersionNumber != query.getVersionNumber() );
        return query;
    }
}
