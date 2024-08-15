package it.unibz.inf.ontop.query.unfolding.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractQueryMergingTransformer;
import it.unibz.inf.ontop.query.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * See {@link QueryUnfolder.Factory} for creating a new instance.
 */
public class TwoPhaseQueryUnfolder extends AbstractIntensionalQueryMerger implements QueryUnfolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwoPhaseQueryUnfolder.class);

    private final Mapping mapping;
    private final CoreUtilsFactory coreUtilsFactory;

    private final CoreSingletons coreSingletons;

    /**
     * See {@link QueryUnfolder.Factory#create(Mapping)}
     */
    @AssistedInject
    private TwoPhaseQueryUnfolder(@Assisted Mapping mapping, IntermediateQueryFactory iqFactory,
                                  CoreUtilsFactory coreUtilsFactory,
                                  CoreSingletons coreSingletons) {
        super(iqFactory);
        this.mapping = mapping;
        this.coreUtilsFactory = coreUtilsFactory;
        this.coreSingletons = coreSingletons;
    }

    @Override
    protected IQTree optimize(IQTree tree) {
        long before = System.currentTimeMillis();
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(tree.getKnownVariables());
        FirstPhaseQueryMergingTransformer firstPhaseTransformer = new FirstPhaseQueryMergingTransformer(mapping, variableGenerator, coreSingletons);

        // NB: no normalization at that point, because of limitation of getPossibleVariableDefinitions implementation
        // (Problem with join strict equality and condition)
        IQTree partiallyUnfoldedIQ = tree.acceptTransformer(firstPhaseTransformer);
        LOGGER.debug("First phase query unfolding time: {}", System.currentTimeMillis() - before);

        if (!firstPhaseTransformer.areIntensionalNodesRemaining())
            return partiallyUnfoldedIQ;

        return executeSecondPhaseUnfolding(partiallyUnfoldedIQ, variableGenerator);
    }

    @Override
    protected AbstractQueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
        throw new MinorOntopInternalBugException("This method should not be called");
    }


    protected IQTree executeSecondPhaseUnfolding(IQTree partiallyUnfoldedIQ, VariableGenerator variableGenerator){
        long before = System.currentTimeMillis();
        AbstractQueryMergingTransformer secondPhaseTransformer = new SecondPhaseQueryMergingTransformer(
                partiallyUnfoldedIQ.getPossibleVariableDefinitions(), mapping, variableGenerator, coreSingletons);
        IQTree unfoldedIQ = partiallyUnfoldedIQ.acceptTransformer(secondPhaseTransformer);
        LOGGER.debug("Second phase query unfolding time: {}", System.currentTimeMillis() - before);
        return unfoldedIQ;
    }

}
