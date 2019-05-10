package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.optimizer.FlattenLifter;
import it.unibz.inf.ontop.iq.optimizer.InnerJoinOptimizer;
import it.unibz.inf.ontop.iq.optimizer.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinOptimizer;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.iq.optimizer.LevelUpOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 *
 */
@Singleton
public class FixedPointJoinLikeOptimizer implements JoinLikeOptimizer {

    private static final Logger log = LoggerFactory.getLogger(FixedPointJoinLikeOptimizer.class);
    private final InnerJoinOptimizer joinOptimizer;
    private final LeftJoinOptimizer leftJoinOptimizer;
    private final IQConverter iqConverter;

    @Inject
    private FixedPointJoinLikeOptimizer(InnerJoinOptimizer joinOptimizer, LeftJoinOptimizer leftJoinOptimizer,
                                        IQConverter iqConverter){
        this.joinOptimizer = joinOptimizer;
        this.leftJoinOptimizer = leftJoinOptimizer;
        this.iqConverter = iqConverter;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        UUID conversionVersion = UUID.randomUUID();
        boolean converged;
        do {

            UUID oldVersionNumber;
            do {
                oldVersionNumber = query.getVersionNumber();
                query = leftJoinOptimizer.optimize(query);
                log.debug("New query after left join optimization: \n" + query.toString());

                query = joinOptimizer.optimize(query);
                log.debug("New query after join optimization: \n" + query.toString());

            } while (oldVersionNumber != query.getVersionNumber());

            converged = (conversionVersion == query.getVersionNumber());
            if (!converged) {
                query = liftBinding(query);
                conversionVersion = query.getVersionNumber();
            }

        } while (!converged);
        return query;
    }

    private IntermediateQuery liftBinding(IntermediateQuery query) throws EmptyQueryException {
        IQ iq = iqConverter.convert(query);
        return iqConverter.convert(iq.liftBinding(), query.getDBMetadata(), query.getExecutorRegistry());
    }
}
